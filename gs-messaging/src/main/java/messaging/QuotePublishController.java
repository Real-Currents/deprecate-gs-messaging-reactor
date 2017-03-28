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

        /* Create a process id for this request, simply by
         * adding system time in milliseconds to 'id' param
         */
        int id = Math.abs((int) System.currentTimeMillis() << 1) +
                ((quoteId.toString() != "Optional.empty")? Integer.valueOf(quoteId.get()) : this.id.get());
        if (this.id.get() <= id) { this.id.set(id); this.id.incrementAndGet(); }

        JsonObject jsonQuote = new JsonObject("{ \"id\": "+ id +", \"method\": \"getSpringQuote\" }");
        JsonRpc20Request quoteRequest;
        MessageConsumer<String> quoteRetrievalListener = eventBus.consumer("quote.retriever"+ id);

        DeferredResult<Quotation> result = new DeferredResult<Quotation>();
        ListenableFuture<Quotation> fQuotation = null;

        eventBus.publish("quote.request", jsonQuote.encode());

        quoteRetrievalListener.handler(message -> {
            quoteRetrievalListener.unregister(res -> {
                if (res.succeeded()) {
                    System.out.println("Quote retrieval complete.");
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
                public void onSuccess(Quotation quotation) {
                    result.setResult(quotation);
                }

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
