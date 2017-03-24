package pcm.state.conditions;

import teaselib.State;

public class TimeTo extends TimeCondition {

    public TimeTo(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long elapsedMillis) {
        if (durationMillis == -State.INDEFINITELY) {
            return true;
        } else if (durationMillis == State.INDEFINITELY) {
            return false;
        } else {
            return elapsedMillis < durationMillis;
        }
    }
}
