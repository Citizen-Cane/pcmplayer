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
        final Date date;
        if (offset.toLowerCase().startsWith("inf")
                || offset.toLowerCase().startsWith("+inf")) {
            date = new Date(Long.MAX_VALUE);
            logger.info("Setting time " + n + " to +INF (" + offset + ")");
        } else if (offset.toLowerCase().startsWith("-inf")) {
            // Values must actually be positive,
            // and Long.MIN_VALUE would lead to an overflow later on
            date = new Date(0);
            logger.info("Setting time " + n + " to -INF (" + offset + ")");
        } else {
            long now = state.getTimeMillis();
            long offset = new Duration(this.offset).getTimeSpanMillis();
            date = new Date(now + offset);
            logger.info("Setting time " + n + " to " + new Date(now).toString()
                    + (this.offset != None ? " + " + this.offset + " = "
                            + new Date(now + offset) : ""));
        }
        state.setTime(n, date);
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}
