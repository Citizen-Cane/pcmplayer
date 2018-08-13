package pcm.state.conditions;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.ConditionRange;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;
import teaselib.util.DurationFormat;

public abstract class TimeCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(TimeCondition.class);

    protected final int n;
    private final DurationFormat duration;

    public TimeCondition(int n, String duration) {
        this.n = n;
        this.duration = new DurationFormat(duration);
    }

    protected abstract boolean predicate(long setTimeSeconds, long elapsedSeconds, long durationSeconds);

    @Override
    public boolean isTrueFor(ScriptState state) {
        long nowSeconds = state.getTimeMillis() / 1000;
        long currentTimeMillis = state.getTimeMillis();
        long setTimeSeconds = state.getTime(n);
        long elapsedSeconds = nowSeconds - setTimeSeconds;
        long durationSeconds = duration.toSeconds();
        boolean result = predicate(setTimeSeconds, elapsedSeconds, durationSeconds);
        log(currentTimeMillis, setTimeSeconds, elapsedSeconds, result);
        return result;
    }

    protected void log(long currentTimeMillis, long setTimeSeconds, long elapsedSeconds, boolean result) {
        if (logger.isInfoEnabled()) {
            logger.info("{} {}: setTime={}, duration={} , now={}, elapsed ={} -> {}", getClass().getSimpleName(), n,
                    new Date(setTimeSeconds * 1000), duration, new Date(currentTimeMillis),
                    DurationFormat.toString(elapsedSeconds, TimeUnit.SECONDS), result);
        }
    }

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        return conditionRange.contains(n);
    }

    @Override
    public String toString() {
        return " " + n + " " + duration;
    }
}
