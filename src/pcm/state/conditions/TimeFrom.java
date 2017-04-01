package pcm.state.conditions;

public class TimeFrom extends TimeCondition {

    public TimeFrom(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long setTimeSeconds, long elapsedSeconds,
            long durationSeconds) {
        return elapsedSeconds >= durationSeconds;
    }
}