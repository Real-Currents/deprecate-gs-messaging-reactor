package messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by revlin on 2/25/17.
 */
@Service
public class QuotePublisher {

//    @Autowired
//    EventBus eventBus;
//
//    @Autowired
//    CountDownLatch latch;
//
//    public void publishQuotes (int quotes) throws InterruptedException {
//        long startTime = System.currentTimeMillis();
//
//        AtomicInteger counter = new AtomicInteger(1);
//
//        System.err.println("Number of quotes to publish: "+ quotes);
//
//        for (int i=0; i<quotes; i++) eventBus.notify("quotes", Event.wrap(counter.getAndIncrement()));
//
//        latch.await();
//
//        System.err.println("Elapsed time: "+ (System.currentTimeMillis() - startTime) +"ms");
//        System.err.println("Average time: "+ (System.currentTimeMillis() - startTime)/quotes +"ms");
//    }
}
