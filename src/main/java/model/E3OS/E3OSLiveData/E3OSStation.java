
package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;


public class E3OSStation {
    

    @JsonProperty("StationID")
    private int StationID;

    @JsonProperty("StationName")
    private String StationName;

    @JsonProperty("ShortName")
    private String ShortName;

    @JsonProperty("IsEnabled")
    private Boolean IsEnabled;

    @JsonProperty("SupervisorID")
    private int SupervisorID;

    @JsonProperty("Supervisor")
    private String Supervisor;

    @JsonProperty("CustomerID")
    private int CustomerID;

    @JsonProperty("CustomerName")
    private String CustomerName;

    @JsonProperty("InstallationID")
    private int InstallationID;

    @JsonProperty("InstallationName")
    private String InstallationName;

    @JsonProperty("JobNumber")
    private String JobNumber;
    
    @JsonProperty("CommissionStatusText")
    private String CommissionStatusText;

    @JsonProperty("RowVer")
    private String RowVer;

    
    public int getStationID() {
        return StationID;
    }

    public String getStationName() {
        return this.StationName;
    }

    public String getShortName() {
        return ShortName;
    }

    public Boolean getIsEnabled() {
        return IsEnabled;
    }

    public int getSupervisorID() {
        return SupervisorID;
    }

    public int getCustomerID() {
        return CustomerID;
    }

    public int getInstallationID() {
        return InstallationID;
    }

    public String getInstallationName() {
        return InstallationName;
    }

    public String getJobNumber() {
        return JobNumber;
    }

    public String getCommissionStatusText() {
        return CommissionStatusText;
    }

    public String getRowVer() {
        return RowVer;
    }

}