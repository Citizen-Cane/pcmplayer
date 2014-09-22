package pcm.state.conditions;

import java.util.Date;

import pcm.model.Duration;
import pcm.state.Condition;
import pcm.state.State;

public class TimeFrom implements Condition {

	final int n;
	final String timeFrom;
	
	public TimeFrom(int n, String timeFrom) {
		this.n = n;
		this.timeFrom = timeFrom;
	}

	@Override
	public boolean isTrueFor(State state) {
		long now = state.getTime() * 1000; // milliseconds
		Date setTime = state.getTime(n);
		if (setTime != null)
		{
			long elapsedDuration = now - setTime.getTime();
			long requestedDuration = new Duration(timeFrom).getTime();
			if (elapsedDuration < requestedDuration)
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return n + " " + timeFrom; 
	}
}
