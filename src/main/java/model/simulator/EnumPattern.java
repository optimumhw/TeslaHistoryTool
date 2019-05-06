
package model.simulator;

import java.util.ArrayList;
import java.util.List;

public enum EnumPattern {
    notSpecified("notSpecified", 0),
    square("square", 1 ),
    linear("linear", 2),
    saw("saw", 3),
    sine("sine", 4);

    private String name;
    private int dropDownIndex;

    EnumPattern(String name, int dropDownIndex) {
        this.name = name;
        this.dropDownIndex = dropDownIndex;

    }

    public String getName() {
        return this.name;
    }

    public int getDropDownIndex() {
        return this.dropDownIndex;
    }

    static public List<String> getLevelNames() {
        List<String> names = new ArrayList<>();
        for (EnumPattern res : EnumPattern.values()) {
            names.add(res.getName());
        }
        return names;
    }

    static public EnumPattern getEnumFromName(String name) {
        for (EnumPattern res : EnumPattern.values()) {
            if (res.getName().compareTo(name) == 0) {
                return res;
            }
        }
        return notSpecified;
    }
}
