
package model.DataPoints;

import java.util.ArrayList;
import java.util.List;


public enum EnumPointTypes {
    CALCULATED("calculated", 1),
    SPARSE("sparse", 2),
    RAW("raw", 3),
    ALARM("alarm", 4);

    private final String name;
    private final int dropDownIndex;

    EnumPointTypes(String name, int dropDownIndex) {
        this.name = name;
        this.dropDownIndex = dropDownIndex;
    }

    public String getName() {
        return this.name;
    }
    
    public int getDropDownIndex(){
        return this.dropDownIndex;
    }

    static public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (EnumPointTypes pointType : EnumPointTypes.values()) {
            names.add(pointType.getName());
        }
        return names;
    }
    
    static public EnumPointTypes getPointTypeFromName( String name ){
        for (EnumPointTypes pointType : EnumPointTypes.values()) {
            if( pointType.getName().compareTo(name) == 0 ){
                return pointType;
            }
        }
        return null;
        
    }
}

