/**
 * 
 */
package pcm.state.conditions;

import java.util.Date;

import pcm.model.Duration;
import pcm.state.Condition;
import pcm.state.State;
import teaselib.TeaseLib;

/**
 * @author someone
 *
 */
public abstract class TimeCondition implements Condition {

    protected final int n;
    protected final long timeSpanMillis;

    public TimeCondition(int n, String timeSpan) {
        super();
        this.n = n;
        this.timeSpanMillis = new Duration(timeSpan).getTimeSpanMillis();
    }

    protected abstract boolean predicate(long elapsedTimeSpan);

    @Override
    public boolean isTrueFor(State state) {
        long now = state.getTimeMillis();
        Date setTime = state.getTime(n);
        if (setTime != null) {
            long elapsedTimeSpan = now - setTime.getTime();
            boolean result = predicate(elapsedTimeSpan);
            log(setTime, elapsedTimeSpan, result);
            return result;
        } else {
            throw new RuntimeException("setTime not called on action " + n);
        }
    }

    protected void log(Date setTime, long elapsedTimeSpan, boolean result) {
        TeaseLib.instance().log.info(getClass().getSimpleName()
                + ": setTime = " + setTime.toString() + ", duration = "
                + toString(timeSpanMillis) + "(" + timeSpanMillis
                + ") , now = " + new Date(System.currentTimeMillis())
                + ", elapsed = " + elapsedTimeSpan + "ms -> " + result);
    }

    @Override
    public String toString() {
        return " " + n + " " + toString(timeSpanMillis);
    }

    public static String toString(long timeSpanMillis) {
        long h = Math.floorDiv(timeSpanMillis, 60 * 60 * 1000);
        long m = Math.floorDiv(timeSpanMillis - h * 60 * 60 * 1000, 60 * 1000);
        long s = Math.floorDiv(timeSpanMillis - h * 60 * 60 * 1000 - m * 60
                * 1000, 1000);
        return String.format("%02d:%02d\"%02d", h, m, s);
    }
}
