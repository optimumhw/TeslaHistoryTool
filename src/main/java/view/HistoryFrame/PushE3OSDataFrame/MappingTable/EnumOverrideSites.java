
package view.HistoryFrame.PushE3OSDataFrame.MappingTable;

import java.util.ArrayList;
import java.util.List;


public enum EnumOverrideSites {

    SUZHOU("Suzhou CN Plant"),
    CORNELIA("Cornelia Plant"),
    LATINA("Latina IT Plant"),
    MANATI("Manati Plant"),
    BIOCORK("Cork IE Plant"),
    UNKONWN("Unknown");

    private final String friendlyName;


    EnumOverrideSites(String name) {
        this.friendlyName = name;
    }

    public static EnumOverrideSites getEnumFromName(String siteName) {

        for (EnumOverrideSites v : EnumOverrideSites.values()) {
            if (v.friendlyName.equalsIgnoreCase(siteName) ) {
                return v;
            }
        }
        return UNKONWN;
    }


    public String getFriendlyName() {
        return this.friendlyName;
    }
    

    public static List<String> getSiteNames() {
        List<String> names = new ArrayList<>();
        for (EnumOverrideSites v : EnumOverrideSites.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}