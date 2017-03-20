package messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static reactor.bus.selector.Selectors.$;

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

    @RequestMapping(value="", method= RequestMethod.GET)
    public DeferredResult<Quotation> restQuote () {
        AtomicInteger id = new AtomicInteger(0);
        ListenableFuture<Quotation> fQuotation = null;
        QuotePublishListener listener;
        DeferredResult<Quotation> result = new DeferredResult<Quotation>();
        long startTime = System.currentTimeMillis();

        System.err.println( id.getAndIncrement() +": "+ fQuotation);

        try {
            fQuotation = quoteService.getQuotation();
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


        return result;
    }
}
