package pcm.state.conditions;

import java.util.Date;

import pcm.model.Duration;
import pcm.state.Condition;
import pcm.state.State;
import teaselib.TeaseLib;

public class TimeFrom implements Condition {

    final int n;
    final String timeFrom;

    public TimeFrom(int n, String timeFrom) {
        this.n = n;
        this.timeFrom = timeFrom;
    }

    @Override
    public boolean isTrueFor(State state) {
        long now = state.getTime() * 1000; // milliseconds
        Date setTime = state.getTime(n);
        if (setTime != null) {
            long elapsedDuration = now - setTime.getTime();
            long requestedDuration = new Duration(timeFrom).getTime();
            TeaseLib.log("TimeFrom: setTime = " + setTime.toString()
                    + ", duration = " + timeFrom + "(" + requestedDuration
                    + ") , now = " + new Date(System.currentTimeMillis())
                    + ", elapsed = " + elapsedDuration + "ms");
            if (elapsedDuration < requestedDuration) {
                TeaseLib.log("-> early");
                return false;
            }
        } else {
            TeaseLib.log("Warning - setTime not called on action " + n);
            return false;
        }
        TeaseLib.log("-> late");
        return true;
    }

    @Override
    public String toString() {
        return n + " " + timeFrom;
    }
}
