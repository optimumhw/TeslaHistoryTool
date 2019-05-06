
package model.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;


public class RequestsResponses {
    
    private final Map< DateTime, RRObj> map;
    
    public RequestsResponses(){
        map = new HashMap<>();
    }
    
    public void clear(){
        this.map.clear();
    }
    
    public void addRequest( RRObj rrObj ){
        map.put( rrObj.getTimestamp(), rrObj );
    }
    
    public List<DateTime> getTimestampsInOrder(){
        
        List<DateTime> timestamps = new ArrayList<>();
        
        for( DateTime ts : map.keySet()){
            
            timestamps.add( ts );
            
        }
        
        Collections.sort(timestamps);
        
        return timestamps;
    }
    
    public RRObj getObj( DateTime timestamp ){
        return map.get(timestamp);
    }
    
}
