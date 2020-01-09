package model.E3OS.E3OSLiveData;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class E3OSDataPoint {

    @JsonProperty("SiteID")
    private int SiteID;

    @JsonProperty("id")
    private int id;

    @JsonProperty("xid")
    private String xid;

    @JsonProperty("rlp")
    private Boolean rlp;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("units")
    private String units;

    @JsonProperty("isdecimalpct")
    private Boolean isdecimalpct;

    @JsonProperty("datapointname")
    private String datapointname;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("abbr")
    private String abbr;

    @JsonProperty("isenabled")
    private Boolean isenabled;

    @JsonProperty("securitytaskid")
    private String securitytaskid;

    @JsonProperty("tz1abbr")
    private String tz1abbr;

    @JsonProperty("tz1offset")
    private int tz1offset;

    @JsonProperty("tz2abbr")
    private String tz2abbr;

    @JsonProperty("tz2offset")
    private int tz2offset;

    @JsonProperty("aggr")
    private String aggr;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("site")
    private String site;

    @JsonProperty("installation")
    private String installation;

    @JsonProperty("station")
    private String station;

    @JsonProperty("scope")
    private String scope;

    public int getSiteID() {
        return SiteID;
    }

    public int getId() {
        return this.id;
    }

    public String getXid() {
        return xid;
    }

    public Boolean getRlp() {
        return rlp;
    }

    public String getDesc() {
        return desc;
    }

    public String getUnits() {
        return units;
    }

    public Boolean getIsDecimalPct() {
        return isdecimalpct;
    }

    public String getDataPointName() {
        return datapointname;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAbbr() {
        return abbr;
    }

    public Boolean getIsEnabled() {
        return isenabled;
    }

    public String getSecurityTaskId() {
        return securitytaskid;
    }

    public String getTz1Abbr() {
        return tz1abbr;
    }

    public int getTz1Offset() {
        return tz1offset;
    }

    public String getTz2Abbr() {
        return tz2abbr;
    }

    public int getTz2Offset() {
        return tz2offset;
    }

    public String getAggr() {
        return aggr;
    }

    public String getCustomer() {
        return customer;
    }

    public String getSite() {
        return site;
    }

    public String getInstallation() {
        return installation;
    }

    public String getStation() {
        return station;
    }

    public String getScope() {
        return scope;
    }

}
