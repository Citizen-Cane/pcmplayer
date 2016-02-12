package pcm.state.commands;

import java.util.Date;

import pcm.model.Duration;
import pcm.state.Command;
import pcm.state.State;

public class SetTime implements Command {

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
    public void execute(State state) {
        final Date date;
        if (offset.toLowerCase().startsWith("inf")) {
            date = new Date(Integer.MAX_VALUE);
            state.player.teaseLib.log
                    .info("Setting time " + n + " to " + offset);
        } else if (offset.toLowerCase().startsWith("+inf")) {
            date = new Date(Integer.MIN_VALUE);
            state.player.teaseLib.log
                    .info("Setting time " + n + " to " + offset);
        } else if (offset.toLowerCase().startsWith("-inf")) {
            date = new Date(Integer.MIN_VALUE);
            state.player.teaseLib.log
                    .info("Setting time " + n + " to " + offset);
        } else {
            long now = state.getTimeMillis();
            long offset = new Duration(this.offset).getTimeSpanMillis();
            date = new Date(now + offset);
            state.player.teaseLib.log.info("Setting time " + n + " to "
                    + new Date(now).toString()
                    + (this.offset != None
                            ? " + " + this.offset + " = " + (now + offset)
                            : ""));
        }
        state.setTime(n, date);
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}
