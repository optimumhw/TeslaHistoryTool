package model.DataPoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

public class ComboHistories {

    private final List<DateTime> comboTimestamps;
    private final List<String> comboPointNames;
    //private final Map< String, Integer> comboPointNameToValueIndex;
    private final Map< DateTime, List< Object>> comboTimestampToValuesArray;

    public ComboHistories(HistoryQueryResults fiveMinuteResults, HistoryQueryResults hourResults) {
        comboTimestamps = new ArrayList<>();
        comboPointNames = new ArrayList<>();
        //comboPointNameToValueIndex = new HashMap<>();
        comboTimestampToValuesArray = new HashMap<>();

        //int pointIndex = 0;
        if (fiveMinuteResults != null) {
            for (String pointName : fiveMinuteResults.getPointNames()) {
                comboPointNames.add(pointName);
                //comboPointNameToValueIndex.put(pointName, pointIndex++);
            }
        }

        if (hourResults != null) {
            for (String pointName : hourResults.getPointNames()) {
                comboPointNames.add(pointName);
                //comboPointNameToValueIndex.put(pointName, pointIndex++);
            }
        }

        if (fiveMinuteResults != null) {
            for (DateTime timestamp : fiveMinuteResults.getTimestamps()) {
                comboTimestamps.add(timestamp);
            }
        }
        if (hourResults != null) {
            for (DateTime timestamp : hourResults.getTimestamps()) {
                if (!comboTimestamps.contains(timestamp)) {
                    comboTimestamps.add(timestamp);
                }
            }

        }

        for (DateTime timestamp : comboTimestamps) {
            List<Object> comboValues = new ArrayList<>();

            if (fiveMinuteResults != null) {
                Map<DateTime, List<Object>> fiveMinValuesAtTimeStamp = fiveMinuteResults.getTimeStampToValuesArray();
                comboValues.addAll(fiveMinValuesAtTimeStamp.get(timestamp));
            }

            if (hourResults != null) {
                if (hourResults.getTimeStampToValuesArray().containsKey(timestamp)) {
                    List<Object> hourValues = hourResults.getTimeStampToValuesArray().get(timestamp);
                    comboValues.addAll(hourValues);
                } else {
                    for (int i = 0; i < hourResults.getPointNames().size(); i++) {
                        comboValues.add("nodata");
                    }

                }
            }

            comboTimestampToValuesArray.put(timestamp, comboValues);
        }

    }

    public List<DateTime> getTimestamps() {
        return comboTimestamps;
    }

    public List<String> getPointNames() {
        return comboPointNames;
    }

    public Map< DateTime, List< Object>> getTimeStampToValuesArray() {
        return comboTimestampToValuesArray;
    }

}
