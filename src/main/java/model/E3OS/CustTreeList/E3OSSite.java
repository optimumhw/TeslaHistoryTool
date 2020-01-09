package model.E3OS.CustTreeList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class E3OSSite {

    @JsonProperty("SiteID")
    private int siteID;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ShortName")
    private String shortName;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("CustomerID")
    private int CustomerID;

    public int getSiteID() {
        return siteID;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getCustomerID() {
        return CustomerID;
    }

}
