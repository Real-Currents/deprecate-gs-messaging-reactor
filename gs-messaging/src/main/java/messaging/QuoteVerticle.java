package messaging;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by revlin on 3/26/17.
 */
@Service
public class QuoteVerticle extends AbstractVerticle {

    @Autowired
    EventBus eventBus;

    // Synchronous verticle start routine
    public void start () {
        MessageConsumer<String> quoteRequestListener = eventBus.consumer("quote.request");

        System.err.println("Verticle Deployed");

        quoteRequestListener.handler(message -> {
            System.out.println( "Quote requested: "+ message.body());
        });

    }

    // Synchronous verticle stop routine
    public void stop () {
        System.err.println("Verticle Undeployed");
    }
}
