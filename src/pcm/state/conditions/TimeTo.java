package pcm.state.conditions;

import teaselib.State;

public class TimeTo extends TimeCondition {

    public TimeTo(int n, String timeSpan) {
        super(n, timeSpan);
    }

    @Override
    protected boolean predicate(long elapsedSeconds, long durationSeconds) {
        if (durationSeconds == -State.INDEFINITELY) {
            return true;
        } else if (durationSeconds == State.INDEFINITELY) {
            return false;
        } else {
            return elapsedSeconds <= durationSeconds;
        }
    }
}
