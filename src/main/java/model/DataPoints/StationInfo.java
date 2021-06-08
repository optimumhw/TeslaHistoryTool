package model.DataPoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StationInfo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("activationCode")
    private String activationCode;

    @JsonProperty("activatedAt")
    private String activatedAt;

    @JsonProperty("expiresAt")
    private String expiresAt;

    @JsonProperty("siteId")
    private String siteId;

    @JsonProperty("e3osShortName")
    private String e3osShortName;

    @JsonProperty("salesforceId")
    private String salesforceId;

    @JsonProperty("commissionedAt")
    private String commissionedAt;

    @JsonProperty("commissionStatus")
    private String commissionStatus;

    @JsonProperty("siteName")
    private String shortName;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("plantId")
    private String plantId;

    @JsonProperty("baselineEnabled")
    private boolean baselineEnabled;

    @JsonProperty("regenerationAllowed")
    private boolean regenerationAllowed;

    @JsonProperty("atomEnabled")
    private boolean atomEnabled;

    @JsonProperty("plantDiagnosticsEnabled")
    private boolean plantDiagnosticsEnabled;

    @JsonProperty("productType")
    private String productType;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("address")
    private String address;

    @JsonProperty("timeZone")
    private String timeZone;

    @JsonProperty("latitude")
    private String latitude;

    @JsonProperty("longitude")
    private String longitude;

    @JsonProperty("communicationProtocol")
    private String communicationProtocol;

    @JsonProperty("points")
    private List<CoreDatapoint> datapoints;

    @JsonProperty("equipments")
    private List<Equipment> equipments;

    @JsonProperty("circuits")
    private Object circuits;

    @JsonProperty("controlGroups")
    private Object controlGroups;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getSalesforceId() {
        return salesforceId;
    }

    public String getcommissionedAt() {
        return commissionedAt;
    }

    public String getcommissionStatus() {
        return commissionStatus;
    }

    public String getShortName() {
        return shortName;
    }

    public String getE3osName() {
        return e3osShortName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPlantID() {
        return plantId;
    }

    public boolean getBaselineEnabled() {
        return baselineEnabled;
    }

    public boolean getRegenerationAllowed() {
        return regenerationAllowed;
    }

    public boolean getAtomEnabled() {
        return atomEnabled;
    }

    public boolean getPlantDiagnosticsEnabled() {
        return plantDiagnosticsEnabled;
    }

    public String getProductType() {
        return productType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public List<CoreDatapoint> getDatapoints() {
        return datapoints;
    }

    public List<Equipment> getequipments() {
        return equipments;
    }

    public String getCommunicationProtocol() {
        return communicationProtocol;
    }

    public Object getCircuits() {
        return circuits;
    }

    public Object getControlGroups() {
        return controlGroups;
    }
}
