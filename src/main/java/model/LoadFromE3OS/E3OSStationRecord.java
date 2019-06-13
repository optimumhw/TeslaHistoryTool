package model.LoadFromE3OS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class E3OSStationRecord {

    /*
    customerid	CustShortName	siteid	SiteShortName	installationid	InstShortName	stationid	StationShortName
81	GT	272	GT	175	10	175	10
2	IBM	3	101	3	101LP	3	101LP1
     */
    private int customerId;
    private String CustShortName;
    private int siteId;
    private String siteShortName;
    private int installationId;
    private String instShortName;
    private int stationId;
    private String stationShortName;

    public E3OSStationRecord(ResultSet rs) {

        try {
            this.customerId = rs.getInt("customerid");
            this.CustShortName = rs.getString("CustShortName");
            this.siteId = rs.getInt("siteid");
            this.siteShortName = rs.getString("SiteShortName");
            this.installationId = rs.getInt("installationid");
            this.instShortName = rs.getString("InstShortName");
            this.stationId = rs.getInt("stationid");
            this.stationShortName = rs.getString("StationShortName");

        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int GetCustomerId() {
        return this.customerId;
    }

    public String GetCustomerName() {
        return this.CustShortName;
    }

    public int GetSiteId() {
        return this.siteId;
    }

    public String GetSiteShortName() {
        return this.siteShortName;
    }

    public int GetInstallationId() {
        return this.installationId;
    }

    public String GetInstShortName() {
        return this.instShortName;
    }

    public int GetStationId() {
        return this.stationId;
    }

    public String GetStationShortName() {
        return this.stationShortName;
    }

}
