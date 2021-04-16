package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class LiveDatapoint {

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

    @JsonProperty("editable")
    private Boolean editable;

    @JsonProperty("minimumResolution")
    private String minimumResolution;

    @JsonProperty("type")
    private String pointType;

    @JsonProperty("ownerType")
    private String ownerType;

    @JsonProperty("ownerId")
    private String ownerId;

    @JsonProperty("unitOfMeasurement")
    private String unitOfMeasurement;

    @JsonProperty("rollupAggregation")
    private String rollupAggregation;
    
    @JsonProperty("hourlySubAggregation")
    private String hourlySubAggregation;
    
    @JsonProperty("hardwareIntegrationSource")
    private String hardwareIntegrationSource;
    
   
    @JsonProperty("values")
    private List<Object> values;

    @JsonProperty("timestamps")
    private List<String> timestamps;

    public String getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getCustomerId() {
        return customerId;
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

    public String getCalculation() {
        return calculation;
    }

    public Boolean getEditable() {
        return editable;
    }

    public String getMinimumResolution() {
        return minimumResolution;
    }

    public String getPointType() {
        return pointType;
    }

    public Object getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public String getRollupAggregation() {
        return rollupAggregation;
    }
    
    public String getHardwareIntegrationSource() {
        return hardwareIntegrationSource;
    }
    
    public List<Object> getValues() {
        return values;
    }

    public List<String> getTimestamps() {
        return timestamps;
    }

}
