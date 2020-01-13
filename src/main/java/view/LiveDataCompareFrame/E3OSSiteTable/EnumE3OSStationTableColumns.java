package view.LiveDataCompareFrame.E3OSSiteTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumE3OSStationTableColumns {

    CustomerID(0, "CustID", 50),
    CustomerName(1, "CustomerName", 300),
    InstallationID(2, "InstID", 50),
    InstallationName(3, "InstName", 100),
    StationID(4, "StatID", 50),
    StationName(5, "StationName", 300),
    IsEnabled(6, "IsEnabled", 100),
    SupervisorID(7, "SuperID", 50),
    Supervisor(8, "Supervisor", 200);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumE3OSStationTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumE3OSStationTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumE3OSStationTableColumns v : EnumE3OSStationTableColumns.values()) {
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
        for (EnumE3OSStationTableColumns v : EnumE3OSStationTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}
