package model.TTT;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.DataPoints.LiveDatapoint;
import model.LoadFromE3OS.TeslaDataPointUpsert;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TTTDataPointUpsertRequest {

    @JsonProperty("list_of_points")
    private final List<TeslaDataPointUpsert> listOfPoints;

    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZoneUTC();

    public TTTDataPointUpsertRequest(List<TeslaDataPointUpsert> listOfPoints) {
        this.listOfPoints = listOfPoints;
    }

    public TTTDataPointUpsertRequest(List<LiveDatapoint> historyPoints, Map< String, String> fromIDToToIDMap) {
        listOfPoints = new ArrayList<>();

        for (LiveDatapoint historyPoint : historyPoints) {

            int timeStampIndex = 0;

            for (String timeStamp : historyPoint.getTimestamps()) {

                Object val = historyPoint.getValues().get(timeStampIndex);

                TeslaDataPointUpsert dpUpsert = new TeslaDataPointUpsert(
                        fromIDToToIDMap.get(historyPoint.getId()),
                        val,
                        timeStamp);

                listOfPoints.add(dpUpsert);

            }
        }
    }

    public List<TeslaDataPointUpsert> getListOfPoints() {
        return this.listOfPoints;
    }
}
