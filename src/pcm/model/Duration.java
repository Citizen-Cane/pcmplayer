package pcm.model;

public class Duration {
    private final String duration;
    private final long timeMillis;

    public Duration(long timeMillis) {
        this.timeMillis = timeMillis;
        this.duration = toString(timeMillis);
    }

    public static String toString(long timeMillis) {
        if (timeMillis == Long.MAX_VALUE) {
            return "INF";
        } else if (timeMillis == Long.MIN_VALUE) {
            return "-INF";
        } else {
            String sign = timeMillis < 0 ? "-" : "";
            long absoluteTimeMillis = Math.abs(timeMillis);
            long h = Math.floorDiv(absoluteTimeMillis, 60 * 60 * 1000);
            long m = Math.floorDiv(absoluteTimeMillis - h * 60 * 60 * 1000,
                    60 * 1000);
            long s = Math.floorDiv(
                    absoluteTimeMillis - h * 60 * 60 * 1000 - m * 60 * 1000,
                    1000);
            return String.format(sign + "%02d:%02d\"%02d", h, m, s);
        }
    }

    public Duration(String duration) {
        this.duration = duration;
        timeMillis = parse(duration);
    }

    /**
     * Get the number of milliseconds of this duration
     * 
     * @return The duration in milli seconds
     */
    public long getTimeSpanMillis() {
        return timeMillis;
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
        if ("INF".equalsIgnoreCase(duration)) {
            return teaselib.State.INDEFINITELY;
        } else if ("-INF".equalsIgnoreCase(duration)) {
            return -teaselib.State.INDEFINITELY;
        } else {
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
                minutes = Integer.parseInt(
                        duration.substring(doubleColonPos + 1, doubleQuotePos));
                seconds = Integer
                        .parseInt(duration.substring(doubleQuotePos + 1));
            } else {
                hours = Integer.parseInt(duration.substring(0, doubleColonPos));
                minutes = Integer
                        .parseInt(duration.substring(doubleColonPos + 1));
                seconds = 0;
            }
            return sign * (3600 * hours + 60 * minutes + seconds) * 1000;
        }
    }

    @Override
    public String toString() {
        return duration;
    }
}
