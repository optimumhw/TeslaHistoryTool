package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class E3osPermission {

    @JsonProperty("custSid")
    private String custSid;

    @JsonProperty("siteSid")
    private String siteSid;

    @JsonProperty("custName")
    private String custName;

    @JsonProperty("siteName")
    private String siteName;

    @JsonProperty("role")
    private String role;

    public String getCustSid() {
        return custSid;
    }

    public String getSiteSid() {
        return siteSid;
    }

    public String getCustName() {
        return custName;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getRole() {
        return role;
    }
}

