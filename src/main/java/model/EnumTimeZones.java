
package model;

import java.util.ArrayList;
import java.util.List;

public enum EnumTimeZones {
    UTC("UTC"),
    Chihuahua("America/Chihuahua"),
    Denver("America/Denver"),
    Los_Angeles("America/Los_Angeles"),
    New_York("America/New_York"),
    Phoenix("America/Phoenix"),
    Tokyo("Asia/Tokyo");

    private final String timeZoneName;

    EnumTimeZones( String timeZoneName )
    {
        this.timeZoneName = timeZoneName;
    }
    
    public String getTimeZoneName(){
        return this.timeZoneName;
    }
    
    static public List<String> getTimeZoneNames(){
        List<String> timeZoneNames = new ArrayList<>();
        for( EnumTimeZones tz : EnumTimeZones.values()){
          timeZoneNames.add(tz.getTimeZoneName() );
        }
        return timeZoneNames;
    }
    
    static public EnumTimeZones getTimeZoneFromName( String timeZoneName ){
        for( EnumTimeZones tz : EnumTimeZones.values() ){
            if( tz.getTimeZoneName().compareTo(timeZoneName) == 0 ){
                return tz; 
            }
        }
        return null;
    }
    
    
}
