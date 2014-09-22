package pcm.model;

public class Duration {
	final long time;

	public Duration(String duration)
	{
		time = parse(duration);
	}
	
	public long getTime() {
		return time;
	}

	static public boolean validate(String duration)
	{
		// Fixed format hh:mm"ss
		return (duration.length() == 8 &&
				duration.charAt(2) == ':' &&
				duration.charAt(5) == '"');
	}

	/**
	 * Parse a PCM duration artgument into seconds starting from 1970 (to be compatibvle with java.util.Date)
	 * @param timeFrom miliseconds of the duration
	 * @return
	 */
	private long parse(String duration)
	{
		long hours = Integer.parseInt(duration.substring(0, 1));
		long minutes = Integer.parseInt(duration.substring(3, 4));
		long seconds =  Integer.parseInt(duration.substring(6, 7));
		return (3600 * hours  + 60 * minutes  + seconds) * 1000;
	}

}
