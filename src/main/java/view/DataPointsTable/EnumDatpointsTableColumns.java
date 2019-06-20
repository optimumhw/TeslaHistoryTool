
package view.DataPointsTable;

import java.util.ArrayList;
import java.util.List;

public enum EnumDatpointsTableColumns {

    ShortName(0,"ShortName", 200),
    Sub(1,"Subscribed", 100),
    LiveValue(2,"Live", 100),
    ID(3,"ID", 300),
    Name(4,"Name", 250),
    UOM(5,"UOM", 100),
    OwnerType(6,"OwnerType", 100),
    Created(7,"Created", 250),
    Updated(8,"Updated", 250),
    OwnerID(9,"OwnerId", 300),
    PointType(10,"Type", 100),
    Calculation(11,"Calculation",250),
    Editable(12,"Editable",50),
    Reso(13,"MinResolution", 100),
    Rollup(14,"RollupAgreg", 100);

    private final String friendlyName;
    private final int columnNumber;
    private final int width;

    EnumDatpointsTableColumns(int columnNumber, String name, int width) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
        this.width = width;
    }

    public static EnumDatpointsTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumDatpointsTableColumns v : EnumDatpointsTableColumns.values()) {
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
        for (EnumDatpointsTableColumns v : EnumDatpointsTableColumns.values()) {
            names.add(v.getFriendlyName());
        }
        return names;
    }
}
