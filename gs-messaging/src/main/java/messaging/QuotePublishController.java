package messaging;

import com.maponics.common.jsonrpc.core.JsonRpc20Request;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/**
 * Created by revlin on 2/25/17.
 */
@Component
@RestController
@RequestMapping("/")
public class QuotePublishController {

    @Autowired
    private Vertx vertx;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private QuoteService quoteService;

    private AtomicInteger id = new AtomicInteger(0);

    @RequestMapping(value={"", "/", "/{quoteId}"}, method= RequestMethod.GET)
    public @ResponseBody ResponseEntity<DeferredResult<Quotation>> restQuote (@PathVariable Optional<String> quoteId) {

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

        DeferredResult<Quotation> result = new DeferredResult<Quotation>();
        ListenableFuture<Quotation> fQuotation = null;

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
        });

        try {
            fQuotation = quoteService.getQuotation(id);
            fQuotation.addCallback(new ListenableFutureCallback<Quotation>() {

                @Override
                public void onSuccess(Quotation quotation) { result.setResult(quotation); }

                @Override
                public void onFailure(Throwable throwable) {
                    result.setErrorResult(throwable.getMessage());
                }
            });
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        return new ResponseEntity<DeferredResult<Quotation>>(result, HttpStatus.OK);
    }
}
