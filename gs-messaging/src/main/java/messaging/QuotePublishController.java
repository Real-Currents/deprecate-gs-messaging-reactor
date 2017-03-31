package messaging;

import com.maponics.common.jsonrpc.core.JsonRpc20Reply;
import com.maponics.common.jsonrpc.core.JsonRpc20Request;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

//import static reactor.bus.selector.Selectors.$;
//import reactor.bus.EventBus;
import io.vertx.core.eventbus.EventBus;

import javax.annotation.PostConstruct;
import javax.management.JMException;

/**
 * Created by John H on 2/25/17.
 */
@Component
@RestController
public class QuotePublishController {

    @Autowired
    private EventBus eventBus;

    private AtomicInteger id = new AtomicInteger(0);

    private void runDeferred(DeferredResult<ResponseEntity<JsonRpc20Reply>> deferred) throws InterruptedException {
        Thread.sleep(3000);
        JsonRpc20Reply jsonReply = JsonRpc20Reply.reply(id.incrementAndGet() +"", new JsonObject("{ \"result\": \"deferred result is set\" }").getMap());
        deferred.setResult(new ResponseEntity<JsonRpc20Reply>(jsonReply, HttpStatus.OK));
    }

    @RequestMapping("/deferred")
    public @ResponseBody DeferredResult<ResponseEntity<JsonRpc20Reply>> tryDeferred() {
        final DeferredResult<ResponseEntity<JsonRpc20Reply>> deferredResult = new DeferredResult<>();
        (new Thread(/*new Runnable() {*/
            //@Override
            /*public void run*/ () -> {
                try {
                    runDeferred(deferredResult);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        /*}*/)).start();
        System.err.println("Processing deferredResult in a different thread.");

        return deferredResult;
    }

    @RequestMapping(value={"", "/", "/{quoteId}"}, method= RequestMethod.GET)
    public @ResponseBody DeferredResult<ResponseEntity<JsonRpc20Reply>> restQuote (@PathVariable Optional<String> quoteId) {
        final DeferredResult<ResponseEntity<JsonRpc20Reply>> deferredResult = new DeferredResult<>();

        /* Create an internal process id for this request, simply
         * by adding system time in milliseconds to 'id' param
         */
        int id = ((quoteId.toString() != "Optional.empty")? Integer.valueOf(quoteId.get()) : this.id.incrementAndGet());
        if (this.id.get() <= id) { this.id.set((id <= 11)? id: 0); }

        long requestId = (Math.abs((int) System.currentTimeMillis() << 8) & 0xFFFFFF00) + id;

        Map<String, Object> jsonParams = new HashMap<>();
        jsonParams.put("quoteId", id+"");

        JsonRpc20Request quoteRequest = new JsonRpc20Request(requestId+"", "getSpringQuote", jsonParams);
        MessageConsumer<String> quoteRetrievalListener = eventBus.consumer("quote.retriever"+ requestId);

        eventBus.publish("quote.request", quoteRequest.toJson());

        quoteRetrievalListener.handler(message -> {
            quoteRetrievalListener.unregister(res -> {
                if (res.succeeded()) {
                    System.out.println("Quote retrieval complete.\n");
                } else {
                    System.out.println("Un-registration failed!");
                }
            });
            System.out.println( "Quote received: "+ message.body());

            //JsonRpc20Reply jsonReply = JsonRpc20Reply.fromJson(message.body().getBytes());
            JsonRpc20Reply jsonReply;
            JsonObject jsonQuote = new JsonObject(message.body());

            try {
                jsonReply = JsonRpc20Reply.reply(jsonQuote.getString("id"), jsonQuote.getJsonObject("result").getMap());

            } catch (NullPointerException e) {
                System.err.println(e.getMessage());
                jsonReply = JsonRpc20Reply.internalError(requestId +"", e.getMessage());

            }

            deferredResult.setResult(new ResponseEntity<JsonRpc20Reply>(jsonReply, HttpStatus.OK));
        });

        return deferredResult;
    }
}
