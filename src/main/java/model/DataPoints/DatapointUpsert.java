
package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatapointUpsert {
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("timestamp")
    private String timestamp;
   
    public DatapointUpsert(){
        
    }
    
    @JsonIgnore
    public DatapointUpsert( String id, Object value, String timestamp){
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
