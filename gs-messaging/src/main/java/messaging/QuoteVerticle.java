package messaging;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by revlin on 3/26/17.
 */
@Service
public class QuoteVerticle extends AbstractVerticle {

    @Autowired
    EventBus eventBus;

    private JsonObject postgresConfig = new JsonObject("{ " +
            "\"host\" : \"localhost\", " +
            "\"port\" : 5432, " +
            "\"maxPoolSize\" : 10, " +
            "\"username\" : \"reactive\", " +
            "\"password\" : \"reactive\", " +
            "\"database\" : \"spring_quotes\"" +
        " }");
    private AsyncSQLClient postgresClient;
    private MessageConsumer<String> quoteRequestListener;

    // Synchronous verticle start routine
    public void start () {
        System.err.println("Verticle Deployed");

        postgresClient = PostgreSQLClient.createShared(vertx, postgresConfig);

        quoteRequestListener = eventBus.consumer("quote.request");
        quoteRequestListener.handler(message -> {
            System.out.println( "Quote requested: "+ message.body());

            postgresClient.getConnection(res -> {
                if (res.succeeded()) {
                    System.err.println("Connected to quote db.");
                    SQLConnection postgresConnection = res.result();
                } else {
                    System.err.println("Failed to connect to db: "+ res.cause());
                }
            });
        });

    }

    // Synchronous verticle stop routine
    public void stop () {
        System.err.println("Verticle Undeployed");
    }
}
