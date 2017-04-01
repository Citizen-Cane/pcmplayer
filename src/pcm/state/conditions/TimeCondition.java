package pcm.state.conditions;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.DurationFormat;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public abstract class TimeCondition implements Condition {
    private static final Logger logger = LoggerFactory
            .getLogger(TimeCondition.class);

    protected final int n;
    private final DurationFormat duration;

    public TimeCondition(int n, String duration) {
        this.n = n;
        this.duration = new DurationFormat(duration);
    }

    protected abstract boolean predicate(long setTimeSeconds,
            long elapsedSeconds, long durationSeconds);

    @Override
    public boolean isTrueFor(ScriptState state) {
        long nowSeconds = state.getTimeMillis() / 1000;
        long setTimeSeconds = state.getTime(n);
        long elapsedSeconds = nowSeconds - setTimeSeconds;
        long durationSeconds = duration.toSeconds();
        boolean result = predicate(setTimeSeconds, elapsedSeconds,
                durationSeconds);
        log(setTimeSeconds, elapsedSeconds, durationSeconds, result);
        return result;
    }

    protected void log(long time, long elapsedSeconds, long durationSeconds,
            boolean result) {
        logger.info(getClass().getSimpleName() + " " + n + ": setTime = "
                + new Date(time * 1000).toString() + ", duration = "
                + duration.toString() + "(" + durationSeconds + ") , now = "
                + new Date(System.currentTimeMillis()) + ", elapsed = "
                + DurationFormat.toString(elapsedSeconds, TimeUnit.SECONDS)
                + "(" + elapsedSeconds + ") -> " + result);
    }

    @Override
    public String toString() {
        return " " + n + " " + duration.toString();
    }
}
