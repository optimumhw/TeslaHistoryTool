package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import model.simulator.UpsertPoint;
import model.simulator.patterns.SimulatorPointPattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DatapointUpsertRequest {

    @JsonProperty("list_of_points")
    private final List<DatapointUpsert> listOfPoints;
    
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public DatapointUpsertRequest(List<UpsertPoint> dgPointsList, EnumResolutions res, DateTime mastertartDateTime, DateTime startDateTime, DateTime endDateTime) {
        listOfPoints = new ArrayList<>();

        List<DateTime> timeStamps = getTimeStamps(res, startDateTime, endDateTime);

        for (UpsertPoint simPoint : dgPointsList) {
            
            SimulatorPointPattern spp = new SimulatorPointPattern(mastertartDateTime, simPoint );

            for (DateTime ts : timeStamps) {
                
                Object val = spp.getValue(ts);
                
                DatapointUpsert dpUpsert = new DatapointUpsert(simPoint.getPoint().getId(), val, ts.toString( fmt ));
                listOfPoints.add(dpUpsert);

            }
        }

    }



    private List<DateTime> getTimeStamps(EnumResolutions res, DateTime start, DateTime end) {

        List<DateTime> timeStamps = new ArrayList<>();

        DateTime counter = start;
        while (counter.isBefore(end)) {
            timeStamps.add(counter);
            counter = counter.plusMinutes(res.getMins());
        }
        return timeStamps;
    }


    public List<DatapointUpsert> getListOfPoints() {
        return this.listOfPoints;
    }
}
