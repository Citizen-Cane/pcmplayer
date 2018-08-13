package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.state.Command;
import pcm.state.persistence.ScriptState;
import teaselib.Duration;
import teaselib.util.DurationFormat;

public class SetTime implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SetTime.class);

    final int n;
    final long limit;

    static final String NOW = "00:00\"00";

    public SetTime(int n) {
        this.n = n;
        this.limit = 0;
    }

    public SetTime(int n, String offset) {
        this.n = n;
        if (offset.toLowerCase().startsWith("inf") || offset.toLowerCase().startsWith("+inf")) {
            limit = Long.MAX_VALUE;
        } else if (offset.toLowerCase().startsWith("-inf")) {
            throw new IllegalArgumentException("offset must be equal or greater than " + NOW);
        } else {
            limit = new DurationFormat(offset).toSeconds();
        }

        if (limit < 0) {
            throw new IllegalArgumentException("Duration limit must be 0 or positive: " + Long.toString(limit));
        }
    }

    @Override
    public void execute(ScriptState state) {
        Duration duration = state.player.duration(limit, TimeUnit.SECONDS);
        logger.info("Setting time {} to {}", n, duration);
        state.setTime(n, duration);
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}
