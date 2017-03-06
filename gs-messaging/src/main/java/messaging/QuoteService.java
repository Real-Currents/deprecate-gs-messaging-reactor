package messaging;

/**
 * Created by revlin on 3/6/17.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

/**
 * Created by revlin on 2/25/17.
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
    public ListenableFuture<Quotation> getQuotation () throws InterruptedException {
        System.err.println( "Getting a Spring Quote" );
        String url = String.format("http://gturnquist-quoters.cfapps.io/api/random");
        Quotation quotation = template.getForObject(url, Quotation.class);
        Thread.sleep(1000);
        return new AsyncResult<Quotation>(quotation);
    }

}
