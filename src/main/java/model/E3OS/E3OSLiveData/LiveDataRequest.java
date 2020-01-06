
package model.E3OS.E3OSLiveData;

import java.util.List;
import org.joda.time.DateTime;


public class LiveDataRequest {
    
    private final String dataSourceType = "LiveData";
    private final long ticks;
    private final List<Integer> dataPointIds;
    
    public LiveDataRequest( DateTime minDate, List<Integer> dataPointIds ){
    
        this.ticks = minDate.getMillis() * 1000L;
        this.dataPointIds = dataPointIds;
    
        //{"dataSourceType":"LiveData","minDateTime":637115884007570000,"dataPoints":[120631]}
        //long ticks = DateTime.now().getMillis() * 1000L;
    }

   
}
