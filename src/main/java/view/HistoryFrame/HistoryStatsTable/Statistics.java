
package view.HistoryFrame.HistoryStatsTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DataPoints.HistoryQueryResults;
import org.joda.time.DateTime;


public class Statistics {
    
    private final HistoryQueryResults history;
    private final Map<String, DatapointStatistics > pointNameToStatsMap;
    
    public Statistics( HistoryQueryResults history ){
        
        this.history = history;
        pointNameToStatsMap = new HashMap<>();
        
        int pointIndex = 0;
        for( String pointName : history.getPointNames() ){
            
            List<Object> valuesForThisPoint = new ArrayList<>();
            for( DateTime ts : history.getTimestamps() ){
                
                Object value = history.getTimeStampToValuesArray().get(ts).get(pointIndex);
                
               valuesForThisPoint.add( value );
            }
            pointNameToStatsMap.put(pointName, new DatapointStatistics(valuesForThisPoint) );
            pointIndex++;
        }
    }
    
    public DatapointStatistics getDataPointStatistics( String pointName ){
        return pointNameToStatsMap.get(pointName );
    }
    
    public List<String> getPointNames(){
        return history.getPointNames();
    }
}
    

