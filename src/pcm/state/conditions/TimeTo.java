package pcm.state.conditions;

public class TimeTo extends TimeCondition {

    public TimeTo(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long setTimeSeconds, long elapsedSeconds,
            long durationSeconds) {
        if (setTimeSeconds == Long.MAX_VALUE) {
            return true;
        } else {
            return elapsedSeconds <= durationSeconds;
        }
    }
}
