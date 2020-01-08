
package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class LiveDataResponse {
    
    @JsonProperty("data")
    private List<LiveDataPointAndValue> data;
    
    @JsonProperty("timeStarted")
    private long timeStarted;
    
    
    public List<LiveDataPointAndValue> getData(){
        return data;
    }
    
    public long getTimeStarted(){
        return timeStarted;
    }
      
}