package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CoreDatapoint {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("stationId")
    private String stationId;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("unitOfMeasurement")
    private String unitOfMeasurement;

    @JsonProperty("ownerType")
    private String ownerType;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("ownerId")
    private String ownerId;

    @JsonProperty("type")
    private String pointType;

    @JsonProperty("calculation")
    private String calculation;

    @JsonProperty("editable")
    private Boolean editable;

    @JsonProperty("minimumResolution")
    private String minimumResolution;

    @JsonProperty("rollupAggregation")
    private String rollupAggregation;

    @JsonProperty("hourlySubAggregation")
    private String hourlySubAggregation;

    @JsonProperty("signalType")
    private String signalType;

    @JsonProperty("hardwareAddress")
    private Object hardwareAddress;

    @JsonIgnore
    private boolean subscribedFlag;

    @JsonIgnore
    private Object liveDataValue;

    @JsonIgnore
    private String hardwareIntegrationSource;

    public String getId() {
        return id;
    }

    public String getStationId() {
        return stationId;
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

    public Boolean getEditable() {
        return this.editable;
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

    public String getHardwareIntegrationSource() {
        return hardwareIntegrationSource;
    }

    public String getHourlySubAggregation() {
        return hourlySubAggregation;
    }

    public String getSignalType() {
        return signalType;
    }

    public Object getHardwareAddress() {
        return hardwareAddress;
    }

}
