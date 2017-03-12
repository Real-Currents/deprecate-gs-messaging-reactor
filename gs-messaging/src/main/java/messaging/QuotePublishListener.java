package messaging;

import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;
import reactor.bus.Event;
import reactor.fn.Consumer;

/**
 * Created by JO088HA on 3/5/2017.
 */
public class QuotePublishListener implements Consumer<Event<Quotation>> {
    private ListenableFuture<Quotation> aQuotation;
    private Quotation quotation;

    public QuotePublishListener () {
        super();
    }
    
    public QuotePublishListener (ListenableFuture<Quotation> fQuotation) {
        super();

        aQuotation = fQuotation;
        System.err.println("QuotePublishListener initialized with "+ fQuotation);

        aQuotation = new AsyncResult<Quotation>(this.quotation);
        System.err.println("QuotePublishListener set AsyncResult on "+ fQuotation);
    }
    
    @Override
    public void accept (Event<Quotation> evt) {
        System.err.println("Recieved QuotePublished event "+ evt.toString());
        quotation = evt.getData();
    }
}