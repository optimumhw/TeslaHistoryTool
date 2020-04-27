package model.DataPoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DatapointList.DatapointListItem;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class HistoryQueryResults {

    private final List<DateTime> timeStamps;
    private final List<String> pointNames;
    private final Map< String, Integer> pointNameToValueIndex;
    private final Map< DateTime, List< Object>> timeStampToValuesArray;
    private final DateTimeFormatter zzFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    public HistoryQueryResults(List<LiveDatapoint> retPoints) {

        timeStamps = new ArrayList<>();
        pointNames = new ArrayList<>();
        pointNameToValueIndex = new HashMap<>();
        timeStampToValuesArray = new HashMap<>();

        int numberOfPoints = retPoints.size();

        int dataPointIndex = 0;

        for (LiveDatapoint ldp : retPoints) {
            pointNames.add(ldp.getShortName());
            pointNameToValueIndex.put(ldp.getShortName(), dataPointIndex);

            int timeStampsIndex = 0;

            for (String timeStamp : ldp.getTimestamps()) {

                DateTime ts = DateTime.parse(timeStamp, zzFormat).withZone(DateTimeZone.UTC);

                if (!timeStamps.contains(ts)) {
                    timeStamps.add(ts);
                }

                if (!timeStampToValuesArray.containsKey(ts)) {
                    List<Object> valuesArray = new ArrayList<>();
                    for (int i = 0; i < numberOfPoints; i++) {
                        valuesArray.add("nothing");
                    }
                    timeStampToValuesArray.put(ts, valuesArray);
                }

                List<Object> valuesArray = timeStampToValuesArray.get(ts);
                valuesArray.set(dataPointIndex, ldp.getValues().get(timeStampsIndex));

                timeStampsIndex++;
            }

            dataPointIndex++;
        }

    }

    public HistoryQueryResults(boolean nop, List<DatapointListItem> listOfTeslaPoints) {

        this.pointNames = new ArrayList<>();
        for (DatapointListItem tdp : listOfTeslaPoints) {
            this.pointNames.add(tdp.getShortName());
        }
        this.timeStamps = new ArrayList<>();
        this.pointNameToValueIndex = new HashMap<>();
        this.timeStampToValuesArray = new HashMap<>();

        int pointNameIndex = 0;
        for (String pointName : pointNames) {
            this.pointNameToValueIndex.put(pointName, pointNameIndex);
            pointNameIndex++;
        }

    }

    public HistoryQueryResults(ComboHistories comboHistories) {
        timeStamps = comboHistories.getTimestamps();
        pointNames = comboHistories.getPointNames();
        pointNameToValueIndex = null;
        timeStampToValuesArray = comboHistories.getTimeStampToValuesArray();
    }

    public void appendFrame(HistoryQueryResults histories) {

        for (DateTime ts : histories.getTimestamps()) {

            if (!timeStampToValuesArray.containsKey(ts)) {
                this.timeStamps.add(ts);
                List<Object> valuesArray = new ArrayList<>();
                for (int i = 0; i < pointNames.size(); i++) {
                    valuesArray.add("nothing");
                }
                this.timeStampToValuesArray.put(ts, valuesArray);

            }

            List<Object> frameVals = histories.getTimeStampToValuesArray().get(ts);
            List<Object> masterValues = timeStampToValuesArray.get(ts);
            int frameValIndex = 0;
            for (String pointName : histories.getPointNames()) {
                int masterValueIndex = this.pointNameToValueIndex.get(pointName);
                masterValues.set(masterValueIndex, frameVals.get(frameValIndex));
                frameValIndex++;
            }
        }
    }

    public void addLivePointResult(LiveDatapoint ldp) {

        int timeStampsIndex = 0;

        for (String timeStamp : ldp.getTimestamps()) {

            DateTime ts = DateTime.parse(timeStamp, zzFormat).withZone(DateTimeZone.UTC);

            if (!timeStamps.contains(ts)) {
                timeStamps.add(ts);
            }

            if (!timeStampToValuesArray.containsKey(ts)) {
                List<Object> valuesArray = new ArrayList<>();
                for (int i = 0; i < pointNames.size(); i++) {
                    valuesArray.add("nothing");
                }
                timeStampToValuesArray.put(ts, valuesArray);
            }

            List<Object> valuesArray = timeStampToValuesArray.get(ts);
            valuesArray.set(pointNameToValueIndex.get(ldp.getShortName()), ldp.getValues().get(timeStampsIndex));

            timeStampsIndex++;
        }
    }

    public List<DateTime> getTimestamps() {
        return timeStamps;
    }

    public List<String> getPointNames() {
        return pointNames;
    }

    public Map< DateTime, List< Object>> getTimeStampToValuesArray() {
        return timeStampToValuesArray;
    }

}
