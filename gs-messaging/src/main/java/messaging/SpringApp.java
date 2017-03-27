package messaging;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by revlin on 2/25/17.
 */
@SpringBootApplication
//@Configuration
//@EnableAutoConfiguration
//@ComponentScan
public class SpringApp {

    private static final int DEFAULT_NUMBER_OF_QUOTES = 10;

    @Autowired
    private Vertx vertx;

    @Bean
    public Vertx getVertx () { return Vertx.vertx(); }

    @Autowired
    private EventBus eventBus;

    @Bean
    public EventBus createEventBus () { return vertx.eventBus(); }

    @Autowired
    private QuoteVerticle quoteVerticle;

    @PostConstruct
    public void startVeritcle () {
        DeploymentOptions options = new DeploymentOptions().setWorker(true);

        vertx.deployVerticle(quoteVerticle, options, evt -> {
            System.err.println(evt.result());
        });
    }

    @PreDestroy
    public void stopVerticle () {
        vertx.undeploy(quoteVerticle.getClass().toString(), evt -> {
            System.err.println(evt.result()); });
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext app = SpringApplication.run(SpringApp.class, args);
    }

}