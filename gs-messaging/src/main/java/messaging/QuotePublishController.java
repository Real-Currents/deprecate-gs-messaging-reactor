package messaging;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
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

/**
 * Created by revlin on 2/25/17.
 */
@Component
@RestController
@RequestMapping("/")
public class QuotePublishController {

    @Autowired
    private EventBus eventBus;

//    @Autowired
//    CountDownLatch latch;

//    @Autowired
//    private QuotePublishListener listener;

    @Autowired
    private QuotePublisher publisher;

    @Autowired
    private QuoteService quoteService;

//    @Bean
//    public QuotePublishListener createPublishListener (QuotePublishListener listener) {
//        return new QuotePublishListener(this);
//    }

//    @Bean
//    public QuotePublishListener createPublishListener(QuotePublishListener listener) {
//        return new QuotePublishListener();
//    }

    private AtomicInteger id = new AtomicInteger(0);

    @RequestMapping(value={"", "/", "/{quoteId}"}, method= RequestMethod.GET)
    public @ResponseBody ResponseEntity<DeferredResult<Quotation>> restQuote (@PathVariable Optional<String> quoteId) {
        int id = (quoteId.toString() != "Optional.empty")? Integer.valueOf(quoteId.get()) : this.id.incrementAndGet();
        ListenableFuture<Quotation> fQuotation = null;
        QuotePublishListener listener;
        DeferredResult<Quotation> result = new DeferredResult<Quotation>();
        MessageConsumer<String> quoteRetrievalListener = eventBus.consumer("quote.retriever"+ id);
        long startTime = System.currentTimeMillis();

        System.err.println( id +": "+ fQuotation);

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
