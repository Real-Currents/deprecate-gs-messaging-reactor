package messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by revlin on 2/25/17.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Quotation {

    String type;
    Quote value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Quote getValue() {
        return value;
    }

    public void setValue(Quote value) {
        this.value = value;
    }

}
