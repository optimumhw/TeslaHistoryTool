
package model.DataPoints;

import java.util.ArrayList;
import java.util.List;


public enum EnumResolutions {
    FIVEMINUTE("fiveMinute", 1, 5),
    HOUR("hour", 2, 60),
    DAY("day", 3, 60 * 24),
    WEEK("week", 4, 60 * 24 * 7),
    MONTH("month", 5, 0),
    QUARTER("quarter", 6, 0),
    YEAR("year", 7, 0);

    private final String name;
    private final int dropDownIndex;
    private final int minutes;

    EnumResolutions(String name, int dropDownIndex, int minutes) {
        this.name = name;
        this.dropDownIndex = dropDownIndex;
        this.minutes = minutes;
    }

    public String getName() {
        return this.name;
    }
    
    public int getDropDownIndex(){
        return this.dropDownIndex;
    }

    static public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (EnumResolutions res : EnumResolutions.values()) {
            names.add(res.getName());
        }
        return names;
    }
    
    static public EnumResolutions getResolutionFromName( String name ){
        for (EnumResolutions res : EnumResolutions.values()) {
            if( res.getName().compareTo(name) == 0 ){
                return res;
            }
        }
        return null;
        
    }
    
    public int getMins(){
        return this.minutes;
    }
    
    public long getTicks(){
        return this.minutes * 1000;
    }
}
