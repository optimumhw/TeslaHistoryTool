
package view.HistoryFrame;

import java.util.ArrayList;
import java.util.List;


public enum EnumYears {
    y2016("2016", 0, 2016),
    y2017("2017", 1, 2017),
    y2018("2018", 2, 2018),
    y2019("2019", 3, 2019),
    y2020("2020", 4, 2020);

    private final String name;
    private final int dropDownIndex;
    private final int year;

    EnumYears(String name, int dropDownIndex, int year) {
        this.name = name;
        this.dropDownIndex = dropDownIndex;
        this.year = year;
    }

    public String getName() {
        return this.name;
    }

    public int getDropDownIndex() {
        return this.dropDownIndex;
    }

    public int getYearNumber(){
        return this.year;
    }
    
    
    static public List<String> getYearNames() {
        List<String> names = new ArrayList<>();
        for (EnumYears year : EnumYears.values()) {
            names.add(year.getName());
        }
        return names;
    }

    static public EnumYears getYearFromName(String name) {
        for (EnumYears year : EnumYears.values()) {
            if (year.getName().compareTo(name) == 0) {
                return year;
            }
        }
        return null;
    }
}
