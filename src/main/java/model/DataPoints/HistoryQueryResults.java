package model.DataPoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class HistoryQueryResults {

    private final List<DateTime> timeStamps;
    private final List<String> pointNames;
    private final Map< DateTime, List< Object>> timeStampToValuesArray;
    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    public HistoryQueryResults(List<LiveDatapoint> retPoints) {

        timeStamps = new ArrayList<>();
        pointNames = new ArrayList<>();
        timeStampToValuesArray = new HashMap<>();
       
        int numberOfPoints = retPoints.size();
        
        int dataPointIndex = 0;

        for (LiveDatapoint ldp : retPoints) {
            pointNames.add(ldp.getShortName());
            int timeStampsIndex = 0;
            for (String timeStamp : ldp.getTimestamps()) {

                DateTime ts = DateTime.parse(timeStamp, zzFormat).withZone(DateTimeZone.UTC);

                if (!timeStamps.contains(ts)) {
                    timeStamps.add(ts);
                }
                
                if( !timeStampToValuesArray.containsKey(ts)){
                    List<Object> valuesArray = new ArrayList<>();
                    for( int i=0; i<numberOfPoints; i++){
                        valuesArray.add("nothing");
                    }
                    timeStampToValuesArray.put(ts, valuesArray );
                }
                
                List<Object> valuesArray = timeStampToValuesArray.get(ts);
                valuesArray.set(dataPointIndex, ldp.getValues().get(timeStampsIndex));
                
                timeStampsIndex++;
            }
            dataPointIndex++;
        }
       
    }

    public List<DateTime> getTimestamps() {
        return timeStamps;
    }

    public List<String> getPointNames() {
        return pointNames;
    }

    public Map< DateTime, List< Object>> getTimeStampToValuesArray() {
        return timeStampToValuesArray;
    }

}
