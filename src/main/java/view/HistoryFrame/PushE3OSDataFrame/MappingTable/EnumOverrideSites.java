package view.HistoryFrame.PushE3OSDataFrame.MappingTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumOverrideSites {

    UTMB("UTMB GalvestonEP"),
    Pickle("JJ Pickle LOOP"),
    SUZHOU("Suzhou CN Plant"),
    CHANDLER("Energy Center"),
    CORNELIA("Cornelia Plant"),
    LATINA("Latina IT Plant"),
    MANATI("Manati Plant"),
    BIOCORK("Cork IE Plant"),
    INDEPEND("Independencia CP"),
    VISTAKON_7("Jacksonville CUP"),
    VISTAKON_1("Jacksonville PH1"),
    VISTAKON_PH2("Jacksonville PH2"),
    VISTAKON_RND("Jacksonville RnD"),
    LAJOLLA("La Jolla Plant"),
    SAN_ANGELO("San Angelo LOOP"),
    YALE("Yale CT US CCCP"),
    FORT_WASH("Fort Washington"),
    SPRINGHOUSE_CUP("SpringHouse CUP"),
    BLDG42("Springhouse B42"),
    UNKONWN("Unknown");

    private final String friendlyName;

    EnumOverrideSites(String name) {
        this.friendlyName = name;
    }

    public static EnumOverrideSites getEnumFromName(String siteName) {

        for (EnumOverrideSites v : EnumOverrideSites.values()) {
            if (v.friendlyName.equalsIgnoreCase(siteName)) {
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
