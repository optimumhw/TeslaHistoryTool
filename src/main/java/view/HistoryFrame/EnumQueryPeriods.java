package view.HistoryFrame;

import java.util.ArrayList;
import java.util.List;

public enum EnumQueryPeriods {
    LAST_12_MONTHS("Last 12 Months", 0),
    THIS_YEAR("This Year", 1),
    LAST_30_DAYS("Last 30 Days", 2),
    THIS_MONTH("This Month", 3),
    LAST_7_DAYS("Last 7 Days", 4),
    THIS_WEEK("This Week", 5),
    LAST_24_HOURS("Last 24 Hours", 6),
    TODAY("Today", 7);

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
