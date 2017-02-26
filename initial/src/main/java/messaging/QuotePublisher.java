package messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import reactor.bus.Event;
import reactor.bus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by revlin on 2/25/17.
 */
@Service
@EnableAsync
public class QuotePublisher {

    @Autowired
    EventBus eventBus;

    @Autowired
    CountDownLatch latch;

    @Async
    public void publishQuotes (int quotes) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        AtomicInteger counter = new AtomicInteger(1);

        for (int i=0; i<quotes; i++) eventBus.notify("quotes", Event.wrap(counter.getAndIncrement()));

        latch.await();

        System.err.println("Elapsed time: "+ (System.currentTimeMillis() - startTime) +"ms");
        System.err.println("Average time: "+ (System.currentTimeMillis() - startTime)/quotes +"ms");
    }

    @Async
    public ListenableFuture<ArrayList<Quotation>> publishQuotes (int quotes, ListenableFuture<ArrayList<Quotation>> quotations) {
        HashMap<String, Object> event = new HashMap<String, Object>();
        ArrayList<Quotation> realQuotations = new ArrayList<Quotation>();
        long startTime = System.currentTimeMillis();

        AtomicInteger counter = new AtomicInteger(1);

        System.err.println("Publishing request for "+ quotes +" quotes...");

        try {

            event.put("count", counter.get());
            event.put("quote", realQuotations);

            for (int i = 0; i < quotes; i++) {
                System.err.println(event);
                Thread.sleep(1000);
                eventBus.notify("quotes", Event.wrap(event));
                event.replace("count", counter.incrementAndGet());
            }

            //quotations = new AsyncResult<ArrayList<Quotation>>(realQuotations);

            latch.await();

        } catch (Exception e) { System.err.println(e.getMessage()); }

        System.err.println("Elapsed time: "+ (System.currentTimeMillis() - startTime) +"ms");
        System.err.println("Average time: "+ (System.currentTimeMillis() - startTime)/quotes +"ms");

        return new AsyncResult<ArrayList<Quotation>>(realQuotations);
    }
}
