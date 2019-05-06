package view.StationsTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumStationsTableColumns {

    ID(0, "ID", 250),
    Name(1, "Name", 120),
    ActivationCode(2, "ActivationCode", 275),
    ActivatedAt(3, "ActivatedAt", 100),
    ExpiresAt(4, "ExpiresAt", 100),
    SiteID(5, "SiteID", 275),
    CommissionedAt(6, "CommissionedAt", 100),
    ShortName(7, "ShortName", 100),
    PlantID(8, "PlantID", 100),
    BaselineEnabled(9, "BaselineEnabled", 100),
    RegenertationAllowed(10, "RegenertationAllowed", 100),
    AtomEnabled(11, "AtomEnabled", 100),
    ProductType(12, "ProductType", 100),
    CreatedAt(13, "CreatedAt", 200),
    UpdatedAt(14, "UpdatedAt", 200),
    Address(15, "Address", 100),
    TimeZone(16, "TimeZone", 100),
    Latitude(17, "Latitude", 100),
    Longitude(18, "Longitude", 100);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumStationsTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumStationsTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumStationsTableColumns v : EnumStationsTableColumns.values()) {
            if (v.getColumnNumber() == colNumber) {
                return v;
            }
        }
        return null;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public int getWidth() {
        return this.width;
    }

    public static List<String> getColumnNames() {
        List<String> names = new ArrayList<>();
        for (EnumStationsTableColumns v : EnumStationsTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}
