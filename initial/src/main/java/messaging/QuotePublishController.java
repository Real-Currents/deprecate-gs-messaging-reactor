package messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by revlin on 2/25/17.
 */
@Component
@RestController
@RequestMapping("/")
public class QuotePublishController {

    private static final int DEFAULT_NUMBER_OF_QUOTES = 10;
    
    @Autowired
    private QuotePublisher publisher;

    @Autowired
    CountDownLatch latch;

    @Bean
    public CountDownLatch createLatch() {
        return new CountDownLatch(DEFAULT_NUMBER_OF_QUOTES);
    }

    @RequestMapping(value="")
    public DeferredResult<ArrayList<Quotation>> getDefaultUser () {
        DeferredResult<ArrayList<Quotation>> result = new DeferredResult<ArrayList<Quotation>>();
        ListenableFuture<ArrayList<Quotation>> quotations = null;
        long startTime = System.currentTimeMillis();

        System.err.println( "Processing request to get default number of quotations");

//        try {
            quotations = publisher.publishQuotes(DEFAULT_NUMBER_OF_QUOTES, quotations);

            System.err.println(quotations);

            quotations.addCallback(new ListenableFutureCallback<ArrayList<Quotation>>() {

                @Override
                public void onSuccess(ArrayList<Quotation> quotations) {
                    System.err.println("Finished processing quotes.");
                    result.setResult(quotations);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    result.setErrorResult(throwable.getMessage());
                }
            });
//        } catch (InterruptedException e) {
//            System.err.println(e.getMessage());
//        }

        return result;
    }
}
