package com.glanznig.beepme.data;

import android.content.Context;

/**
 * RandomTimer represents a timer which schedules random beeps. This randomness is either restricted
 * to an interval [min;max] - where the probability of beep occurrence is uniformly distributed
 * within this interval - or to an interval [min;avg;max] - where the probability of
 * the beep occurrence follows a normal (? SELECT!!) distribution reaching highest probability
 * at AVG and reaching zero probability at MIN and MAX.
 */
public class RandomTimer extends Timer {

    public enum TimerStrategy {
        INTERVAL, AVERAGE
    }

    private TimerStrategy strategy;
    private Long min; // in seconds
    private Long avg; // in seconds
    private Long max; // in seconds

    public RandomTimer(Context ctx, TimerStrategy strategy, long min, long max) {
        super(ctx);
        this.strategy = strategy;
        this.min = Long.valueOf(min);
        avg = null;
        this.max = Long.valueOf(max);
    }

    /**
     * Gets the timer strategy of this random timer (average, interval).
     * @return timer strategy
     */
    public TimerStrategy getStrategy() {
        return strategy;
    }

    /**
     * Gets the minimum required distance between two beeps in seconds
     * @return minimum required distance in seconds
     */
    public long getMin() {
        return min.longValue();
    }

    /**
     * Gets the maximum allowed distance between two beeps in seconds
     * @return maximum allowed distance in seconds
     */
    public long getMax() {
        return max.longValue();
    }

    /**
     * Sets the average distance between two beeps in seconds (only if timer strategy is AVERAGE).
     * @param avg average distance between two beeps in seconds
     */
    public void setAvg(long avg) {
        if (strategy.equals(TimerStrategy.AVERAGE)) {
            avg = Long.valueOf(avg);
        }
    }

    /**
     * Gets the average distance between two beeps in seconds
     * @return average distance between two beeps in seconds, or 0L if not set/timer strategy other than AVERAGE
     */
    public long getAvg() {
        if (strategy.equals(TimerStrategy.AVERAGE)) {
            if (avg != null) {
                return avg.longValue();
            }
        }
        return 0L;
    }

    public Beep getNext() {
        return null;
    }

    /**
     * Transforms the Timer object into a string representation (for serialization, persistance).
     * The string has the form 'strategy={average|interval},min=MIN_INTERVAL[,avg=AVG_INTERVAL],max=MAX_INTERVAL,sound={pling|700hz}'.
     * @return string representation of Timer object
     */
    public String toString() {
        String strRep = "strategy=";

        switch (strategy) {
            case INTERVAL:
                strRep += "interval";
                break;
            case AVERAGE:
                strRep += "average";
                break;
        }

        strRep += ",min="+min.longValue();
        if (strategy.equals(TimerStrategy.AVERAGE) && avg != null) {
            strRep += ",avg="+avg.longValue();
        }
        strRep += ",max="+max.longValue();
        strRep += ",sound="+super.getContext().getResources().getResourceEntryName(super.getSound());

        return strRep;
    }

    /**
     * Transforms a string representation of a Timer object (e.g. from storage) into
     * an object. The string has to have the form
     * 'strategy={average|interval},min=MIN_INTERVAL[,avg=AVG_INTERVAL],max=MAX_INTERVAL,sound={pling|700hz}'.
     * With AVG_INTERVAL required if strategy=average.
     * @param objRepresentation string representation of Timer object
     */
    public static RandomTimer fromString(Context ctx, String objRepresentation) {
        if (objRepresentation.toLowerCase()
                .matches("^strategy=(interval|average),min=\\d+(,avg=\\d+)?,max=\\d+,sound=(pling|700hz)$")) {
            String strategy = "";
            String min = "";
            String max = "";
            String avg = "";
            String sound = "";

            String[] splitRep = objRepresentation.split(",");
            for (int i=0; i < splitRep.length; i++) {
                if (splitRep[i].startsWith("strategy")) {
                    strategy = splitRep[i].substring(9);
                }
                else if (splitRep[i].startsWith("min")) {
                    min = splitRep[i].substring(4);
                }
                else if (splitRep[i].startsWith("max")) {
                    max = splitRep[i].substring(4);
                }
                else if (splitRep[i].startsWith("avg")) {
                    avg = splitRep[i].substring(4);
                }
                else if (splitRep[i].startsWith("sound")) {
                    sound = splitRep[i].substring(6);
                }
            }

            TimerStrategy timerStrategy = TimerStrategy.INTERVAL;
            long minVal = 600L;
            long maxVal = 3600L;

            if (strategy.equals("interval")) {
                timerStrategy = TimerStrategy.INTERVAL;
            }
            else if (strategy.equals("average")) {
                timerStrategy = TimerStrategy.AVERAGE;
            }

            if (min != null) {
                minVal = Long.valueOf(min);
            }
            if (max != null) {
                maxVal = Long.valueOf(max);
            }

            RandomTimer randomTimer = new RandomTimer(ctx, timerStrategy, minVal, maxVal);
            if (avg != null) {
                randomTimer.setAvg(Long.valueOf(avg));
            }

            // strategy average must define average value
            if (timerStrategy.equals(TimerStrategy.AVERAGE) && avg == null) {
                return null;
            }

            randomTimer.setSound(sound);

            return randomTimer;
        }
        return null;
    }
}
