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
    private final Duration duration;

    public TimeCondition(int n, String duration) {
        this.n = n;
        this.duration = new Duration(duration);
    }

    protected abstract boolean predicate(long elapsedMillis,
            long durationMillis);

    @Override
    public boolean isTrueFor(ScriptState state) {
        long now = state.getTimeMillis();
        Date setTime = state.getTime(n);
        if (setTime != null) {
            long elapsedMillis = now - setTime.getTime();
            long durationMillis = duration.getTimeSpanMillis();
            boolean result = predicate(elapsedMillis, durationMillis);
            log(setTime, elapsedMillis, durationMillis, result);
            return result;
        } else {
            throw new RuntimeException("setTime not called on action " + n);
        }
    }

    protected void log(Date setTime, long elapsedMillis, long durationMillis,
            boolean result) {
        logger.info(getClass().getSimpleName() + " " + n + ": setTime = "
                + setTime.toString() + ", duration = " + duration.toString()
                + "(" + durationMillis + ") , now = "
                + new Date(System.currentTimeMillis()) + ", elapsed = "
                + Duration.toString(elapsedMillis) + "(" + elapsedMillis
                + ") -> " + result);
    }

    @Override
    public String toString() {
        return " " + n + " " + duration.toString();
    }
}
