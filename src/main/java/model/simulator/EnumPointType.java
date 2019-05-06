package model.simulator;

import java.util.ArrayList;
import java.util.List;

public enum EnumPointType {

    numericType("numeric", 0),
    booleanType("boolean", 1),
    stringType("text", 2);

    private final String edisonName;
    private final int dropDownIndex;

    EnumPointType(String name, int dropDownIndex) {
        this.edisonName = name;
        this.dropDownIndex = dropDownIndex;

    }

    public String getEdisonName() {
        return this.edisonName;
    }
    
        public int getDropDownIndex(){
        return this.dropDownIndex;
    }

    public static EnumPointType getTypeFromName(String name) {
        for (EnumPointType pt : EnumPointType.values()) {
            if (pt.getEdisonName().compareTo(name) == 0) {
                return pt;
            }
        }

        return null;
    }

    static public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (EnumPointType res : EnumPointType.values()) {
            names.add(res.getEdisonName());
        }
        return names;
    }

}
