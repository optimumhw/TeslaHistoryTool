
package view.HistoryFrame.HistoryStatsTable;

import java.util.List;

public class DatapointStatistics {

    List<Object> vals;
    private int countNotNull;
    private int totalCount;
    private double sum;
    private double mean;
    private double stdDev;
    private double max;
    private double min;

    boolean maxSet = false;
    boolean minSet = false;

    public DatapointStatistics(List<Object> vals) {
        this.vals = vals;
        computeStats();
    }

    private void computeStats() {
        this.countNotNull = 0;
        this.totalCount = 0;
        this.sum = 0;
        this.mean = 0;
        this.stdDev = 0;
        this.max = 0;
        this.min = 0;

        maxSet = false;
        minSet = false;

        if (vals == null) {
            return;
        }

        this.totalCount = vals.size();

        for (Object v : vals) {
            if (v == null) {
                continue;
            }

            double temp = 0;
            if (v instanceof Double) {
                temp = (double) v;
            } else if (v instanceof Integer) {

                temp = (int) v;
            } else if (v instanceof Boolean) {

                Boolean flag = (Boolean) v;
                temp += (flag) ? 1.0 : 0;
            } else {
                continue;
            }

            countNotNull++;
            sum += temp;

            if (!minSet || temp < this.min) {
                this.min = temp;
                minSet = true;
            }

            if (!maxSet || temp > this.max) {
                this.max = temp;
                maxSet = true;
            }

        }

        if (this.countNotNull > 0) {
            this.mean = this.sum / this.countNotNull;
        }
        
        
        //compute stddev
        double sumOfSquaredDeviations = 0;
        for (Object v : vals) {
            if (v == null) {
                continue;
            }

            double temp = 0;
            if (v instanceof Double) {
                temp = (double) v;
            } else if (v instanceof Integer) {

                temp = (int) v;
            } else if (v instanceof Boolean) {

                Boolean flag = (Boolean) v;
                temp += (flag) ? 1.0 : 0;
            } else {
                continue;
            }
            
            sumOfSquaredDeviations += Math.pow(temp - mean, 2.0);
        }
        
        double variance = ( Math.abs(countNotNull) > 0) ? sumOfSquaredDeviations/countNotNull : 0;
        this.stdDev = Math.sqrt(variance);
    }

    public int getCount() {
        return this.countNotNull;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public double getSum() {
        return this.sum;
    }

    public double getMean() {
        return this.mean;
    }
    
    public double getStdDev(){
        return this.stdDev;
    }

    public double getMax() {
        return this.max;
    }

    public double getMin() {
        return this.min;
    }

    public void recomputeStatsWithAddtionalValue(Object value) {
        this.vals.add( value );
        computeStats();
    }

}
