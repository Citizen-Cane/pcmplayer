package pcm.state.conditions;

import java.util.Date;

import pcm.model.Duration;
import pcm.state.Condition;
import pcm.state.State;
import teaselib.TeaseLib;

public class TimeTo implements Condition {

    final int n;
    final String timeTo;

    public TimeTo(int n, String timeTo) {
        this.n = n;
        this.timeTo = timeTo;
    }

    @Override
    public boolean isTrueFor(State state) {
        long now = state.getTimeMillis();
        Date setTime = state.getTime(n);
        if (setTime != null) {
            long elapsedDuration = now - setTime.getTime();
            long requestedDuration = new Duration(timeTo).getTime();
            TeaseLib.instance().log.info(getClass().getSimpleName()
                    + ": setTime = " + setTime.toString() + ", duration = "
                    + timeTo + "(" + requestedDuration + ") , now = "
                    + new Date(System.currentTimeMillis()) + ", elapsed = "
                    + elapsedDuration + "ms");
            if (elapsedDuration >= requestedDuration) {
                TeaseLib.instance().log.info("-> late");
                return false;
            }
        } else {
            TeaseLib.instance().log
                    .info("Warning - setTime not called on action " + n);
            return false;
        }
        TeaseLib.instance().log.info("-> early");
        return true;
    }

    @Override
    public String toString() {
        return n + " " + timeTo;
    }
}
