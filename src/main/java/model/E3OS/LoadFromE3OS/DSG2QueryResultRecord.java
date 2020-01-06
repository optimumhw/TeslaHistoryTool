
package model.E3OS.LoadFromE3OS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class DSG2QueryResultRecord {     

    private int id;
    private String pointName;
    private DateTime timestamp;
    private Object pointValue;
    private int tz;

    public int getId() {
        return id;
    }
    
    public String getPointName(){
        return pointName;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public Object getValue() {
        return pointValue;
    }

    public int getTZOffset() {
        return tz;
    }

    
    public DSG2QueryResultRecord( Map<Integer,String> indexToPointNameMap, ResultSet rs ){

        try {
            this.id = rs.getInt("id");
            this.pointName = indexToPointNameMap.get(id);
            String timeStr = rs.getString("time");
            this.pointValue = rs.getObject("value");
            this.tz = rs.getInt("tz");
            
            DateTimeFormatter fromFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SS");
            DateTime temp = DateTime.parse( timeStr, fromFormat ).withZone(DateTimeZone.UTC);
            
       
            
            this.timestamp = temp.minuteOfDay().roundFloorCopy();
                                                                                               
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
