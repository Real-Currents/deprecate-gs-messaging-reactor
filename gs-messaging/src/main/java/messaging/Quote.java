package messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonArray;

/**
 * Created by John H on 2/25/17.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Quote {

    Long id;
    String quote;

    public Quote () { super(); }

    public Quote (JsonArray springQuote) {
        /* Convert results array [id, quote]
         * into a quote object.
         */
        this.id = (long) springQuote.getInteger(0);
        this.quote = (String) springQuote.getString(1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

}
