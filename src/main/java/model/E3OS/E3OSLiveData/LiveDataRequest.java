
package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.joda.time.DateTime;


public class LiveDataRequest {
    
    @JsonProperty("dataSourceType")
    private final String dataSourceType = "LiveData";
    
    @JsonProperty("minDateTime")
    private final long ticks;
    
    @JsonProperty("dataPoints")
    private final List<Integer> dataPointIds;
    
    @JsonIgnore
    public LiveDataRequest( DateTime minDate, List<Integer> dataPointIds ){
    
        this.ticks = minDate.getMillis() * 1000L;
        this.dataPointIds = dataPointIds;
    
        //{"dataSourceType":"LiveData","minDateTime":637115884007570000,"dataPoints":[120631]}
        //long ticks = DateTime.now().getMillis() * 1000L;
    }
    
    

   
}
