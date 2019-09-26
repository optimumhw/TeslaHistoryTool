package view.StationsTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumStationsTableColumns {

    CustName(0, "Cust", 120),
    Name(1, "Name", 120),
    ID(2, "ID", 300),
    TimeZone(3, "TimeZone", 100),
    ActivationCode(4, "ActivationCode", 275),
    ActivatedAt(5, "ActivatedAt", 100),
    ExpiresAt(6, "ExpiresAt", 100),
    SiteID(7, "SiteID", 300),
    CommissionedAt(8, "CommissionedAt", 100),
    ShortName(9, "ShortName", 100),
    PlantID(10, "PlantID", 100),
    BaselineEnabled(11, "BaselineEnabled", 100),
    RegenertationAllowed(12, "RegenertationAllowed", 100),
    AtomEnabled(13, "AtomEnabled", 100),
    ProductType(14, "ProductType", 100),
    CreatedAt(15, "CreatedAt", 200),
    UpdatedAt(16, "UpdatedAt", 200),
    Address(17, "Address", 100),
    Latitude(18, "Latitude", 100),
    Longitude(19, "Longitude", 100);

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
