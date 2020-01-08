package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LiveDataPointAndValue {

    @JsonProperty("id")
    private int id;

    @JsonProperty("value")
    private double value;

    @JsonProperty("lastUpdate")
    private long lastUpdate;

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}
