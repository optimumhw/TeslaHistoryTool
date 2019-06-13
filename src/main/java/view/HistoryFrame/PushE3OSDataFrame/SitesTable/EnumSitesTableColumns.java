package view.HistoryFrame.PushE3OSDataFrame.SitesTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumSitesTableColumns {

    CustomerID(0, "CustomerID", 100),
    CustShortName(1, "CustShortName", 200),
    SiteId(2, "SiteId", 400),
    SiteShortName(3, "SiteShortName", 200),
    InstId(4, "InstId", 50),
    InstShortName(5, "InstShortName", 300),
    StationId(6, "StationId", 50),
    StationShortName(7, "StationShortName", 300);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumSitesTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumSitesTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumSitesTableColumns v : EnumSitesTableColumns.values()) {
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
        for (EnumSitesTableColumns v : EnumSitesTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}
