package model.LoadFromE3OS;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TeslaDataPointUpsertRequest {

    @JsonProperty("list_of_points")
    private final List<TeslaDataPointUpsert> listOfPoints;

    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZoneUTC();

    public TeslaDataPointUpsertRequest(List<TeslaDataPointUpsert> listOfPoints) {
        this.listOfPoints = listOfPoints;
    }

    public TeslaDataPointUpsertRequest(List<DSG2QueryResultRecord> e3osHistory, Map< String, MappingTableRow> e3osNameToMappingTableRowMap) {
        listOfPoints = new ArrayList<>();

        for (DSG2QueryResultRecord dsg2Record : e3osHistory) {

            DateTime timeStamp = dsg2Record.getTimestamp();

            Object val = dsg2Record.getValue();

            if (val instanceof Boolean) {
                Boolean tempBool = (Boolean) val;
                val = (tempBool) ? 1.0 : 0.0;
            }

            MappingTableRow mtr = e3osNameToMappingTableRowMap.get(dsg2Record.getPointName());

            TeslaDataPointUpsert dpUpsert = new TeslaDataPointUpsert(
                    mtr.getTeslaID(),
                    val,
                    timeStamp.toString(fmt));
            listOfPoints.add(dpUpsert);

        }
    }

    public List<TeslaDataPointUpsert> getListOfPoints() {
        return this.listOfPoints;
    }
}
