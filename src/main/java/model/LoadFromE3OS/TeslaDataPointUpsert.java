
package model.LoadFromE3OS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TeslaDataPointUpsert {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("timestamp")
    private String timestamp;

    public TeslaDataPointUpsert() {

    }

    @JsonIgnore
    public TeslaDataPointUpsert(String id, Object value, String timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
