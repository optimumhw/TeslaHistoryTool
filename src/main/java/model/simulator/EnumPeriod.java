
package model.simulator;

import java.util.ArrayList;
import java.util.List;

public enum EnumPeriod {
    notSpecified("notSpecified", 0),
    min("min", 1000 * 60 ),
    min5("min5", 1000 * 60 * 5),
    min15("min15", 1000 * 60 * 15),
    min30("min30", 1000 * 60 * 30),
    hourly("hourly", 1000 * 60 * 60 ),
    halfDay("halfday", 1000 * 60 * 60 * 12),
    day("day", 1000 * 60 * 60 * 24),
    twoday("twoday", 1000 * 60 * 60 * 48),
    week("week", 1000 * 60 * 60 * 24 * 7),
    twoWeek("twoweek", 1000 * 60 * 60 * 24 * 14);
    
    private String name;
    private long ticks;

    EnumPeriod(String name, int ticks) {
        this.name = name;
        this.ticks = ticks;

    }

    public String getName() {
        return this.name;
    }


    static public List<String> getLevelNames() {
        List<String> names = new ArrayList<>();
        for (EnumPeriod res : EnumPeriod.values()) {
            names.add(res.getName());
        }
        return names;
    }

    static public EnumPeriod getEnumFromName(String name) {
        for (EnumPeriod res : EnumPeriod.values()) {
            if (res.getName().compareTo(name) == 0) {
                return res;
            }
        }
        return notSpecified;
    }
    
    public long getTicks(){
        return this.ticks;
    }
    
}
