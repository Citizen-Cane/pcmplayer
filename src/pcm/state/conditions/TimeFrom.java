package pcm.state.conditions;

import teaselib.State;

public class TimeFrom extends TimeCondition {

    public TimeFrom(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long elapsedMillis) {
        if (durationMillis == -State.INFINITE) {
            return false;
        } else if (durationMillis == State.INFINITE) {
            return true;
        } else {
            return elapsedMillis > durationMillis;
        }
    }
}