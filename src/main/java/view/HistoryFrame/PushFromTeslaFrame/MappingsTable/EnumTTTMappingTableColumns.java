
package view.HistoryFrame.PushFromTeslaFrame.MappingsTable;

import java.util.ArrayList;
import java.util.List;


public enum EnumTTTMappingTableColumns {

    MapStatus(0, "MapStatus", 100),
    FromName(1,"FromName", 200),
    FromType(2,"FromType", 100),
    FromID(3, "FromID", 400),
    ToName(4,"ToName", 200),
    ToType(5,"ToType", 100),
    ToID(6, "ToID", 400);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumTTTMappingTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumTTTMappingTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumTTTMappingTableColumns v : EnumTTTMappingTableColumns.values()) {
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
        for (EnumTTTMappingTableColumns v : EnumTTTMappingTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}