
package model.E3OS.LoadFromE3OS;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class DSG2Runner {
    
    private final String host;
    private final String username;
    private final String password;
    
    public DSG2Runner( E3OSConnProperties connProperties ){
        this.host = connProperties.getHost();
        this.username = connProperties.getUsername();
        this.password = connProperties.getPassword();
    }

    public List<DSG2QueryResultRecord> runDSG2Query(String startDate, String endDate, List<DataPointFromSql> points) throws SQLServerException, SQLException, ClassNotFoundException {

        Connection conn = null;
        ResultSet rs = null;
        SQLServerCallableStatement cs = null;

        Map<Integer, String> indexToPointNameMap = new HashMap<>();

        List<DSG2QueryResultRecord> list = new ArrayList<>();
        

        try {

            SQLServerDataTable sourceDataTable = new SQLServerDataTable();
            sourceDataTable.addColumnMetadata("seqNbr", java.sql.Types.INTEGER);
            sourceDataTable.addColumnMetadata("DataPointXID", java.sql.Types.VARCHAR);
            sourceDataTable.addColumnMetadata("DataPointID", java.sql.Types.INTEGER);
            sourceDataTable.addColumnMetadata("Time_AggregateType", java.sql.Types.VARCHAR);
            sourceDataTable.addColumnMetadata("Rollup_AggregateType", java.sql.Types.VARCHAR);

            int index = 1;
            for (DataPointFromSql point : points) {
                String pt = point.getXID() + point.getDatapointName();
                sourceDataTable.addRow(index, pt, null, "Minute", null);
                indexToPointNameMap.put(index, point.getDatapointName());
                index++;
            }

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DriverManager.setLoginTimeout(30);
                        String url = String.format("jdbc:sqlserver://%s;databaseName=oemvmdata;user=%s;password=%s", 
                    host, username, password);
                        
            DriverManager.setLoginTimeout(30);
            conn = DriverManager.getConnection(url);

            String sprocCaller = "{call oemvmdata.fact.DataSeriesGet2 (?,?,?,?,?,?,?,?,?,?,?,?,?,? )}";
            cs = (SQLServerCallableStatement) conn.prepareCall(sprocCaller);

            //DECLARE @FromTime_Local datetime
            java.sql.Timestamp fromTime = getSqlDate(startDate);
            cs.setTimestamp(1, fromTime);

            //DECLARE @ToTime_Local datetime
            java.sql.Timestamp toTime = getSqlDate(endDate);
            cs.setTimestamp(2, toTime);

            //DECLARE @DataPointsOfInterest [fact].[DataPointsOfInterest]
            cs.setStructured(3, "fact.DataPointsOfInterest", sourceDataTable);

            //DECLARE @TimeRange varchar(50)
            cs.setNull(4, java.sql.Types.NULL);

            //DECLARE @TimeInterval varchar(50)
            cs.setString(5, "Minute");

            //DECLARE @CalculatedFromTime datetime
            cs.registerOutParameter(6, java.sql.Types.DATE);

            //DECLARE @CalculatedToTime datetime
            cs.registerOutParameter(7, java.sql.Types.DATE);
            //cs.setString(6, "week");

            //DECLARE @RequestProcessing bit
            cs.setNull(8, java.sql.Types.NULL);

            //DECLARE @RequestGUID uniqueidentifier
            cs.setNull(9, java.sql.Types.NULL);

            //DECLARE @IncludeOutOfBounds bit
            cs.setBoolean(10, false);

            //DECLARE @IncludeUncommissioned bit
            cs.setBoolean(11, true);

            //DECLARE @ForceCacheRefresh bit
            cs.setNull(12, java.sql.Types.NULL);

            //DECLARE @UserName nvarchar(256)
            cs.setString(13, "tkitchen");

            //DECLARE @UserID uniqueidentifier 
            cs.setString(14, "1d355ea9-7b16-4822-9483-1dd34173b5b8");

            boolean resultSetReturned = cs.execute();
            if (resultSetReturned) {
                try (ResultSet rs_Temp = cs.getResultSet()) {
                    while (rs_Temp.next()) {
                        list.add(new DSG2QueryResultRecord(indexToPointNameMap, rs_Temp));
                    }
                }
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
            if (cs != null) {
                try {
                    cs.close();
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

    private static java.sql.Timestamp getSqlDate(String dateString) {
        
        DateTime utc = new DateTime(dateString, DateTimeZone.UTC);
        
        TimeZone localTimeZone = TimeZone.getDefault();
        DateTime localDate = new DateTime(utc, DateTimeZone.forID(localTimeZone.getID())); 
        return new java.sql.Timestamp(localDate.getMillis());
        
        //DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //DateTime ts = DateTime.parse(dateString, fmt);
        //return new java.sql.Timestamp(ts.getMillis());
        
        
    }
}

