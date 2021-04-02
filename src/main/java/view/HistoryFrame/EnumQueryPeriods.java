package view.HistoryFrame;

import java.util.ArrayList;
import java.util.List;

public enum EnumQueryPeriods {
    LAST_MONTH("Last Month", 0),
    LAST_12_MONTHS("Last 12 Months", 1),
    THIS_YEAR("This Year", 2),
    LAST_30_DAYS("Last 30 Days", 3),
    THIS_MONTH("This Month", 4),
    LAST_7_DAYS("Last 7 Days", 5),
    THIS_WEEK("This Week", 6),
    LAST_24_HOURS("Last 24 Hours", 7),
    TODAY("Today", 8);

    private final String queryPeriodName;
    private final int dropDownIndex;

    EnumQueryPeriods(String name, int dropDownIndex) {
        this.queryPeriodName = name;
        this.dropDownIndex = dropDownIndex;
    }

    public String getQueryPeriodName() {
        return this.queryPeriodName;
    }

    public int getDropDownIndex() {
        return this.dropDownIndex;
    }

    static public List<String> getQueryPeriodNames() {
        List<String> names = new ArrayList<>();
        for (EnumQueryPeriods month : EnumQueryPeriods.values()) {
            names.add(month.getQueryPeriodName());
        }
        return names;
    }

    static public EnumQueryPeriods getQueryPeriodFromName(String name) {
        for (EnumQueryPeriods res : EnumQueryPeriods.values()) {
            if (res.getQueryPeriodName().compareTo(name) == 0) {
                return res;
            }
        }
        return null;
    }
}
