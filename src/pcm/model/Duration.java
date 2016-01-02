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
    public long getTime() {
        return time;
    }

    static public boolean validate(String duration) {
        // Fixed format hh:mm"ss
        return (duration.length() == 8 && duration.charAt(2) == ':' && duration
                .charAt(5) == '"');
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
        long hours = Integer.parseInt(duration.substring(0, 2));
        long minutes = Integer.parseInt(duration.substring(3, 5));
        long seconds = Integer.parseInt(duration.substring(6, 8));
        return sign * (3600 * hours + 60 * minutes + seconds) * 1000;
    }

}
