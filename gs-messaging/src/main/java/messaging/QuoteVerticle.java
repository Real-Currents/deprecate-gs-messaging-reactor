package messaging;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.ResultSet;
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

            int requestId = new JsonObject(message.body()).getInteger("id");
            int quoteId = (requestId % 12) + 1;

            postgresClient.getConnection(connectionRes -> {
                if (connectionRes.succeeded()) {
                    System.err.println("Connected to quote db.");
                    System.err.println( "Getting Spring Quote "+ quoteId );

                    SQLConnection postgresConnection = connectionRes.result();
                    JsonArray params = new JsonArray().add(quoteId);

                    postgresConnection.queryWithParams(
                            "SELECT * FROM quotations WHERE id=?",
                            params,
                            queryRes -> {
                                if (queryRes.succeeded()) {
                                    ResultSet results = queryRes.result();
                                    Quote springQuote = new Quote(results.getResults().get(0));
                                    System.err.println(springQuote.getQuote());
                                    eventBus.publish("quote.retriever"+ requestId, requestId +"\n"+ springQuote.getQuote());
                                } else {
                                    System.err.println("Failed to query db: "+ queryRes.cause());
                                }
                            }
                        );

                } else {
                    System.err.println("Failed to connect to db: "+ connectionRes.cause());
                }
            });
        });

    }

    // Synchronous verticle stop routine
    public void stop () {
        System.err.println("Verticle Undeployed");
    }
}
