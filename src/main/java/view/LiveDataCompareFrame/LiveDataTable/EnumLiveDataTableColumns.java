package view.LiveDataCompareFrame.LiveDataTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumLiveDataTableColumns {

    MapStatus(0, "MapStatus", 100),
    PollFlag(1, "PollFlag", 50),
    CoreName(2, "CoreName", 150),
    CoreValue(3, "CoreValue", 100),
    E3OSName(4, "E3OSName", 150),
    E3OSvalue(5, "E3OSvalue", 100),
    CoreType(6, "CoreType", 100),
    CoreID(7, "CoreID", 300),
    E3OSID(8, "E3OSID", 75);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumLiveDataTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumLiveDataTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumLiveDataTableColumns v : EnumLiveDataTableColumns.values()) {
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
        for (EnumLiveDataTableColumns v : EnumLiveDataTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}
