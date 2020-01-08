
package view.LiveDataCompareFrame.E3OSSiteTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumE3OSSitesTableColumns {

    SiteId(0, "SiteId", 50),
    Name(1, "Name", 100),
    ShortName(2, "ShortName", 50),
    CustomerName(3, "CustomerName", 100);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumE3OSSitesTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumE3OSSitesTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumE3OSSitesTableColumns v : EnumE3OSSitesTableColumns.values()) {
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
        for (EnumE3OSSitesTableColumns v : EnumE3OSSitesTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}

