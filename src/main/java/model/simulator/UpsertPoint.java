package model.simulator;

import model.DataPoints.Datapoint;


public class UpsertPoint {

    private Double minValue;
    private Double maxValue;

    private EnumPattern pattern;
    private EnumPeriod period;
    
    private Double offset;
    
    private Datapoint dp;

    public UpsertPoint(Datapoint dp) {
        
        this.dp = dp;
        this.minValue = 0.0;
        this.maxValue = 0.0;
        this.pattern = EnumPattern.notSpecified;
        this.period = EnumPeriod.notSpecified;
        this.offset = 0.0;
        
    }
    
    public Datapoint getPoint(){
        return dp;
    }
    
    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public EnumPeriod getPeriod() {
        return this.period;
    }

    public void setPeriod(EnumPeriod period) {
        this.period = period;
    }

    public EnumPattern getPattern() {
        return this.pattern;
    }

    public void setPattern(EnumPattern pattern) {
        this.pattern = pattern;
    }
    
    public double getOffset() {
        return this.offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

}
