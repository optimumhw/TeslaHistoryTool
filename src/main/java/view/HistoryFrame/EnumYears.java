
package view.HistoryFrame;

import java.util.ArrayList;
import java.util.List;


public enum EnumYears {
    y2018("2018", 0, 2018),
    y2019("2019", 1, 2019),
    y2020("2020", 2, 2020),
    y2021("2021", 3, 2021),
    y2022("2022", 4, 2022),
    y2023("2023", 5, 2023);

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
