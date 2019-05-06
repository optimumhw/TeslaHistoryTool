package model.simulator.patterns;

import model.DataPoints.EnumResolutions;
import model.simulator.UpsertPoint;
import model.simulator.EnumPattern;
import model.simulator.EnumPeriod;
import org.joda.time.DateTime;

public class SimulatorPointPattern {

    private final DateTime startTime;
    private final Double offset;
    private final Double min;
    private final Double max;
    private final EnumPeriod patternPeriod;
    private final EnumPattern enumPattern;

    public SimulatorPointPattern(DateTime startTime, UpsertPoint simPoint ) {
        this.offset = simPoint.getOffset();
        this.min = simPoint.getMinValue();
        this.max = simPoint.getMaxValue();
        this.patternPeriod = simPoint.getPeriod();
        this.startTime = startTime;
        enumPattern = simPoint.getPattern();
    }

    public Object getValue(DateTime timeStamp) {

        switch (enumPattern) {
            case linear: {
                long ticks = timeStamp.getMillis() - startTime.getMillis();
                long ticksInPeriod = patternPeriod.getTicks();
                long startTick = (ticks / ticksInPeriod) * ticksInPeriod;
                Double slope = (ticksInPeriod == 0) ? 0 : (max - min) / ticksInPeriod;
                Double val = min + slope * (ticks - startTick);
                val += offset;
                
                return val;
            }

            case saw: {
                long ticks = timeStamp.getMillis() - startTime.getMillis();
                long ticksInPeriod = patternPeriod.getTicks();
                long startTick = (ticks / ticksInPeriod) * ticksInPeriod;
                Double slope = 0.0;
                Double val = 0.0;
                if (ticksInPeriod != 0) {
                    long halfWay = ((2 * startTick) + ticksInPeriod) / 2;
                    slope = 2 * (max - min) / ticksInPeriod;
                    if (ticks > halfWay) {
                        slope *= -1;
                        val = max + slope * (ticks - (startTick + (ticksInPeriod / 2)));
                    } else {
                        val = min + slope * (ticks - startTick);
                    }
                }
                return val;
            }

            case square: {
                long ticks = timeStamp.getMillis() - startTime.getMillis();
                long ticksInPeriod = patternPeriod.getTicks();
                long startTick = (ticks / ticksInPeriod) * ticksInPeriod;
                long halfWay = ((2 * startTick) + ticksInPeriod) / 2;
                double val = (ticks > halfWay) ? max : min;

                return val;
            }

            case sine: {
                long ticks = timeStamp.getMillis() - startTime.getMillis();
                Double amplitude = Math.abs(max - min) / 2;
                Double val = min + amplitude * (1 + Math.sin(ticks * 2 * Math.PI / patternPeriod.getTicks()));
                return val;
            }
            
            case notSpecified:
            {
                return 0;
            }

        }
        
        return 0;
    }

}
