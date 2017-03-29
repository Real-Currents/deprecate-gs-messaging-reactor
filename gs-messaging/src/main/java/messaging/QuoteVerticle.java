package messaging;

import com.maponics.common.jsonrpc.core.JsonRpc20Reply;
import com.maponics.common.jsonrpc.core.JsonRpc20Error;
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

import java.util.Map;

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

            postgresClient.getConnection(connectionRes -> {
                System.err.println("Connecting to quote db.");

                int requestId = Integer.valueOf((String) new JsonObject(message.body()).getString("id"));
                int quoteId = Integer.valueOf((String) new JsonObject(message.body()).getJsonObject("params").getString("quoteId"));

                if (quoteId > 12) {
                    JsonRpc20Reply jsonError = JsonRpc20Reply.invalidParams(requestId + "",
                            quoteId +" is invalid. There are 12 Spring Quotes in the database."
                    );

                    eventBus.publish("quote.retriever" + requestId, jsonError.toJson());

                } else if (connectionRes.succeeded()) {
                    System.err.println("Getting Spring Quote " + quoteId);

                    SQLConnection postgresConnection = connectionRes.result();
                    JsonArray params = new JsonArray().add(quoteId);

                    //System.err.println("postgresConnection: "+ postgresConnection.toString());

                    postgresConnection.queryWithParams(
                        "SELECT * FROM quotations WHERE id=?",
                        params,
                        queryRes -> {
                            postgresConnection.close();

                            if (queryRes.succeeded()) {
                                ResultSet results = queryRes.result();
                                Quote springQuote = new Quote(results.getResults().get(0));
                                JsonRpc20Reply jsonQuotation = JsonRpc20Reply.reply(requestId + "", JsonObject.mapFrom(springQuote).getMap());

                                System.err.println(springQuote.getQuote());

                                eventBus.publish("quote.retriever" + requestId, jsonQuotation.toJson());

                            } else {
                                JsonRpc20Reply jsonError = JsonRpc20Reply.invalidParams(requestId + "", queryRes.cause().toString());

                                System.err.println("Failed to query db: " + queryRes.cause());

                                eventBus.publish("quote.retriever" + requestId, jsonError.toJson());
                            }
                        }
                    );

                } else {
                    System.err.println("Failed to connect to db: " + connectionRes.cause());
                }

                postgresClient.close();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            });

        });

    }

    // Synchronous verticle stop routine
    public void stop () {
        System.err.println("Verticle Undeployed");
    }
}
