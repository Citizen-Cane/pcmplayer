package pcm.state.conditions;

public class TimeTo extends TimeCondition {

    public TimeTo(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long elapsedTimeSpan) {
        return elapsedTimeSpan < timeSpanMillis;
    }
}
