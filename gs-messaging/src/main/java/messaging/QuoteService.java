package messaging;

/**
 * Created by John H on 3/6/17.
 */

import io.vertx.core.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

/**
 * This is a traditional Spring Boot service. The primary method returns an instance of
 * ListenableFuture, "a Future implementation that adds non-blocking callback-based capabilities."
 */
@Service
public class QuoteService <T> {

    @Autowired
    private final RestTemplate template;

    @Bean
    public RestTemplate getQuoteTemplate () {
        return new RestTemplate();
    }

    public QuoteService (RestTemplateBuilder builder) {
        this.template = builder.build();
    }

    @Async
    public ListenableFuture<Quotation> getQuotation (int id) throws InterruptedException {

        String url = String.format("http://gturnquist-quoters.cfapps.io/api/random");

        Quotation quotation = template.getForObject(url, Quotation.class);

        //Thread.sleep(3000);

        return new AsyncResult<Quotation>(quotation);
    }

}
