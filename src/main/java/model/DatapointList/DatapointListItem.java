package model.DatapointList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatapointListItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("siteId")
    private String siteId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("stationId")
    private String stationId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("calculation")
    private String calculation;

    @JsonProperty("minimumResolution")
    private String minimumResolution;

    @JsonProperty("type")
    private String pointType;

    @JsonProperty("unitOfMeasurement")
    private String unitOfMeasurement;

    @JsonProperty("rollupAggregation")
    private String rollupAggregation;

    public String getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Object getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getCalculation() {
        return calculation;
    }

    public String getMinimumResolution() {
        return minimumResolution;
    }

    public String getPointType() {
        return pointType;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public String getrollupAggregation() {
        return rollupAggregation;
    }

}
