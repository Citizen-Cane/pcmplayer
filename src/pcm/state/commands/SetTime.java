package pcm.state.commands;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.Duration;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class SetTime implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SetTime.class);

    final int n;
    final String offset;

    final static String None = "00:00\"00";

    public SetTime(int n) {
        this.n = n;
        this.offset = None;
    }

    public SetTime(int n, String offset) {
        this.n = n;
        this.offset = offset;
    }

    @Override
    public void execute(ScriptState state) {
        if (offset.toLowerCase().startsWith("inf")
                || offset.toLowerCase().startsWith("+inf")) {
            logger.info("Setting time " + n + " to +INF (" + offset + ")");
            state.setTime(n, state.getTimeMillis(), Long.MAX_VALUE);
        } else if (offset.toLowerCase().startsWith("-inf")) {
            // Values must actually be positive,
            // and Long.MIN_VALUE would lead to an overflow later on
            logger.info("Setting time " + n + " to -INF (" + offset + ")");
            state.setTime(n, 0, 0);
        } else {
            // TODO use timespan to avoid unstable code
            long now = state.getTimeMillis();
            long offset = new Duration(this.offset).getTimeSpanMillis();
            logger.info("Setting time " + n + " to " + new Date(now).toString()
                    + (this.offset != None ? " + " + this.offset + " = "
                            + new Date(now + offset) : ""));
            state.setTime(n, now, offset);
        }
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}
