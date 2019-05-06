package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class HistoryRequest {

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("startAt")
    private String startAt;

    @JsonProperty("endAt")
    private String endAt;

    @JsonProperty("resolution")
    private String resolution;

    @JsonProperty("timeZone")
    private String timeZone;

    public HistoryRequest() {

    }

    @JsonIgnore
    public HistoryRequest(List<String> ids, DateTime startAt, DateTime endAt, String resolution, String timeZone) {

        this.ids = ids;

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        this.startAt = startAt.toString(fmt);
        this.endAt = endAt.toString(fmt);

        this.resolution = resolution;
        this.timeZone = timeZone;

    }

    public List<String> getIds() {
        return ids;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getResolution() {
        return resolution;
    }

    public String getTimeZone() {
        return timeZone;
    }

}
