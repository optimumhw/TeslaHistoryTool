package model.LoadFromE3OS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SiteQuery {

    private final String host;
    private final String username;
    private final String password;

    public SiteQuery() {

        E3OSConnProperties connProperties = new E3OSConnProperties();

        this.host = connProperties.getHost();
        this.username = connProperties.getUsername();
        this.password = connProperties.getPassword();
    }

    public List<E3OSStationRecord> runSiteQuery() {

        List<E3OSStationRecord> list = new ArrayList<>();
        String queryString = "select c.customerid, c.ShortName as 'CustShortName', s.siteid, \n"
                + "s.ShortName as 'SiteShortName', i.installationid, i.ShortName as 'InstShortName', \n"
                + "st.stationid, st.ShortName as 'StationShortName'\n"
                + "from oemvm.dim.Customer c \n"
                + "inner join oemvm.dim.site s on s.CustomerID = c.CustomerID \n"
                + "inner join oemvm.dim.Installation i on i.SiteID = s.SiteID \n"
                + "inner join oemvm.dim.Station st on st.InstallationID = i.InstallationID\n"
                + "order by StationShortName";

        /*
    customerid	CustShortName	siteid	SiteShortName	installationid	InstShortName	stationid	StationShortName
81	GT	272	GT	175	10	175	10
2	IBM	3	101	3	101LP	3	101LP1
         */
        Connection conn = null;
        Statement cstmt = null;
        ResultSet rs = null;
        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = String.format("jdbc:sqlserver://%s;databaseName=oemvmdata;user=%s;password=%s",
                    host,
                    username,
                    password);

            DriverManager.setLoginTimeout(30);
            conn = DriverManager.getConnection(url);
            cstmt = conn.createStatement();

            rs = cstmt.executeQuery(queryString);

            while (rs.next()) {
                list.add(new E3OSStationRecord(rs));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(this.getClass().getName()).log(
                            Level.WARNING, null, ex);
                }
            }
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(this.getClass().getName()).log(
                            Level.WARNING, null, ex);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(this.getClass().getName()).log(
                            Level.WARNING, null, ex);
                }
            }
        }

        return list;
    }

}
