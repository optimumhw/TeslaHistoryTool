package view.DataGenerator;

import java.util.ArrayList;
import java.util.List;

public enum EnumDGTableColumns {

    ShortName(0, "Name"),
    ID(1,"id"),
    MinValue(2, "MinValue"),
    MaxValue(3, "MaxValue"),
    Pattern(4, "Pattern"),
    Period(5, "Period"),
    Offset(6, "Offset"),
    PointType(7, "PointType");

    private final String friendlyName;
    private final int columnNumber;

    EnumDGTableColumns(int columnNumber, String name) {
        this.friendlyName = name;
        this.columnNumber = columnNumber;
    }

    public static EnumDGTableColumns getColumnFromColumnNumber(int colNumber) {

        for (EnumDGTableColumns v : EnumDGTableColumns.values()) {
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

    public static List<String> getColumnNames() {
        List<String> names = new ArrayList<>();

        for (EnumDGTableColumns v : EnumDGTableColumns.values()) {

            names.add(v.getFriendlyName());

        }
        return names;
    }
}
