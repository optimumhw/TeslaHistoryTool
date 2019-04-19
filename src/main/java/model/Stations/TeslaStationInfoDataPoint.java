package model.Stations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeslaStationInfoDataPoint {

    @JsonProperty("id")
    private String id;

    @JsonProperty("ownerType")
    private String ownerType;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("ownerId")
    private String ownerId;

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

    @JsonIgnore
    private boolean subscribedFlag;

    @JsonIgnore
    private Object liveDataValue;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Object getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPointType() {
        return pointType;
    }

    public String getCalculation() {
        return calculation;
    }

    public String getMinimumResolution() {
        return minimumResolution;
    }

    public String getRollupAggregation() {
        return rollupAggregation;
    }

    public void setSubScribedFlag(boolean subscribedFlag) {
        this.subscribedFlag = subscribedFlag;
    }

    public boolean getSubscribedFlag() {
        return subscribedFlag;
    }

    public void setLiveDataValue(Object liveDataValue) {
        this.liveDataValue = liveDataValue;
    }

    public Object getLiveDataValue() {
        return liveDataValue;
    }

}
