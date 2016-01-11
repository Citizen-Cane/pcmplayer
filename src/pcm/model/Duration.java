package pcm.model;

public class Duration {
    private final long time;

    public Duration(String duration) {
        time = parse(duration);
    }

    /**
     * Get the number of milliseconds of this duration
     * 
     * @return The duration in milli seconds
     */
    public long getTimeSpanMillis() {
        return time;
    }

    /**
     * Parse a PCM duration argument into seconds starting from 1970 (to be
     * compatible with java.util.Date)
     * 
     * @param timeFrom
     *            milliseconds of the duration
     * @return
     */
    private static long parse(String duration) {
        long sign = 1;
        if (duration.startsWith("-")) {
            sign = -1;
            duration = duration.substring(1);
        } else if (duration.startsWith("+")) {
            duration = duration.substring(1);
        }
        final long hours;
        final long minutes;
        final long seconds;
        int doubleColonPos = duration.indexOf(':', 0);
        int doubleQuotePos = duration.indexOf('"', 0);
        if (doubleQuotePos >= 3) {
            hours = Integer.parseInt(duration.substring(0, doubleColonPos));
            minutes = Integer.parseInt(duration.substring(doubleColonPos + 1,
                    doubleQuotePos));
            seconds = Integer.parseInt(duration.substring(doubleQuotePos + 1));
        } else {
            hours = Integer.parseInt(duration.substring(0, doubleColonPos));
            minutes = Integer.parseInt(duration.substring(doubleColonPos + 1));
            seconds = 0;
        }
        return sign * (3600 * hours + 60 * minutes + seconds) * 1000;
    }
}
