
package view.HistoryFrame.DatapointsListTable;

import java.util.ArrayList;
import java.util.List;


public enum EnumDataPointsListTableColumns {

    ShortName(0,"ShortName", 200),
    PointType(1,"PointType", 100),
    MinReso(2, "MinReso", 100),
    Rollup(3, "Rollup", 100),
    ID(4,"ID", 300),
    Name(5,"Name", 250),
    SiteId(6, "SiteId", 300);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumDataPointsListTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumDataPointsListTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumDataPointsListTableColumns v : EnumDataPointsListTableColumns.values()) {
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
    
    public int getWidth(){
        return this.width;
    }

    public static List<String> getColumnNames() {
        List<String> names = new ArrayList<>();
        for (EnumDataPointsListTableColumns v : EnumDataPointsListTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}

