
package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Equipment {
   
    @JsonProperty("controlGroupId")
    private String controlGroupId;
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;
    
    @JsonProperty("e3osShortName")
    private String e3osShortName;
    
   
    @JsonProperty("type")
    private String equipmentType;

    @JsonProperty("stationId")
    private String stationId;
    
    @JsonProperty("salesforceId")
    private String salesforceId;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("make")
    private String make;

    @JsonProperty("model")
    private String model;
    
    @JsonProperty("points")
    private List<CoreDatapoint> dataPoints;

    
            
    public String getControlGroupId() {
        return controlGroupId;
    }
    

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }
    
    
    public String gete3osShortName() {
        return e3osShortName;
    }

    public Object getEquipmentType() {
        return equipmentType;
    }

    public String getStationId() {
        return stationId;
    }
    
    public String getSalesforceId(){
        return salesforceId;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public List<CoreDatapoint> getDatapoints() {
        return dataPoints;
    }

}
