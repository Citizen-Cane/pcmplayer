package pcm.state.conditions;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.Duration;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public abstract class TimeCondition implements Condition {
    private static final Logger logger = LoggerFactory
            .getLogger(TimeCondition.class);

    protected final int n;
    protected final long durationMillis;

    public TimeCondition(int n, String duration) {
        super();
        this.n = n;
        if ("INF".equalsIgnoreCase(duration)) {
            this.durationMillis = teaselib.State.INFINITE;
        } else if ("-INF".equalsIgnoreCase(duration)) {
            this.durationMillis = -teaselib.State.INFINITE;
        } else {
            this.durationMillis = new Duration(duration).getTimeSpanMillis();
        }
    }

    protected abstract boolean predicate(long elapsedMillis);

    @Override
    public boolean isTrueFor(ScriptState state) {
        long now = state.getTimeMillis();
        Date setTime = state.getTime(n);
        if (setTime != null) {
            final long elapsedMillis;
            elapsedMillis = now - setTime.getTime();
            boolean result = predicate(elapsedMillis);
            log(setTime, elapsedMillis, result);
            return result;
        } else {
            throw new RuntimeException("setTime not called on action " + n);
        }
    }

    protected void log(Date setTime, long elapsedMillis, boolean result) {
        logger.info(getClass().getSimpleName() + " " + n + ": setTime = "
                + setTime.toString() + ", duration = "
                + toString(durationMillis) + "(" + durationMillis + ") , now = "
                + new Date(System.currentTimeMillis()) + ", elapsed = "
                + toString(elapsedMillis) + " -> " + result);
    }

    @Override
    public String toString() {
        return " " + n + " " + toString(durationMillis);
    }

    public static String toString(long durationMillis) {
        long h = Math.floorDiv(durationMillis, 60 * 60 * 1000);
        long m = Math.floorDiv(durationMillis - h * 60 * 60 * 1000, 60 * 1000);
        long s = Math.floorDiv(
                durationMillis - h * 60 * 60 * 1000 - m * 60 * 1000, 1000);
        return String.format("%02d:%02d\"%02d", h, m, s);
    }
}
