
package model.E3OS.LoadFromE3OS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PointsListQueryRunner {
    
    private final String host;
    private final String username;
    private final String password;
    
    public PointsListQueryRunner(){
        E3OSConnProperties connProperties = new E3OSConnProperties();
        
        this.host = connProperties.getHost();
        this.username = connProperties.getUsername();
        this.password = connProperties.getPassword();
        
    }

    public List<DataPointFromSql> runDataPointsQuery( String stationID ) {

        List<DataPointFromSql> list = new ArrayList<>();

        String xidColName = "DataPointXID";
        String dataPointNameColName = "DataPointName";


        String querySTring = String.format("" +
"            use oemvm;\n" +
"            SELECT [DataPointName]\n" +
"                  ,[DataPointXID]\n" +
"                  ,[PointType]\n" +
"                  ,[CreateTime]\n" +
"                  ,[DisplayName]\n" +
"              FROM [oemvm].[dim].[DataPoint_List]\n" +
"              where StationID=%s",
                stationID );

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

            rs = cstmt.executeQuery(querySTring);

            while (rs.next()) {
                list.add(new DataPointFromSql(rs.getString(xidColName), rs.getString(dataPointNameColName)));
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
