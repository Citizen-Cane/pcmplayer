package pcm.state.commands;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.DurationFormat;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;
import teaselib.Duration;

public class SetTime implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SetTime.class);

    final int n;
    final String offset;

    final static String NOW = "00:00\"00";

    public SetTime(int n) {
        this.n = n;
        this.offset = NOW;
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
            state.setTime(n,
                    state.player.duration(Long.MAX_VALUE, TimeUnit.SECONDS));
        } else if (offset.toLowerCase().startsWith("-inf")) {
            throw new IllegalArgumentException(
                    "offset must be equal or greater than " + NOW);
        } else {
            DurationFormat offset = new DurationFormat(this.offset);
            Duration duration = state.player.duration(offset.toSeconds(),
                    TimeUnit.SECONDS);
            logger.info("Setting time " + n + " to "
                    + new Date(duration.start(TimeUnit.MILLISECONDS)).toString()
                    + (this.offset != NOW
                            ? " + " + this.offset + " = " + this.offset : ""));
            state.setTime(n, duration);
        }
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}
