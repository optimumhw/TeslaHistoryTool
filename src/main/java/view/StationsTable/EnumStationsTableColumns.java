package view.StationsTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumStationsTableColumns {

    ID(0, "ID", 300),
    Name(1, "Name", 120),
    TimeZone(2, "TimeZone", 100),
    ActivationCode(3, "ActivationCode", 275),
    ActivatedAt(4, "ActivatedAt", 100),
    ExpiresAt(5, "ExpiresAt", 100),
    SiteID(6, "SiteID", 300),
    CommissionedAt(7, "CommissionedAt", 100),
    ShortName(8, "ShortName", 100),
    PlantID(9, "PlantID", 100),
    BaselineEnabled(10, "BaselineEnabled", 100),
    RegenertationAllowed(11, "RegenertationAllowed", 100),
    AtomEnabled(12, "AtomEnabled", 100),
    ProductType(13, "ProductType", 100),
    CreatedAt(14, "CreatedAt", 200),
    UpdatedAt(15, "UpdatedAt", 200),
    Address(16, "Address", 100),
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
