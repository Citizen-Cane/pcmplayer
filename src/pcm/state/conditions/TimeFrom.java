package pcm.state.conditions;

import teaselib.State;

public class TimeFrom extends TimeCondition {

    public TimeFrom(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long elapsedSeconds, long durationSeconds) {
        if (durationSeconds == -State.INDEFINITELY) {
            return false;
        } else if (durationSeconds == State.INDEFINITELY) {
            return true;
        } else {
            return elapsedSeconds >= durationSeconds;
        }
    }
}