package pcm.model;

import teaselib.util.Interval;


public class ActionRange extends Interval {
	public ActionRange(int start)
	{
		super(start);
	}

	public ActionRange(int start, int end)
	{
		super(start, end);
	}

	public boolean validate()
	{
		return start <= end;
	}

	@Override
	public String toString() {
		if (start == end)
		{
			return "Range " + start;
		}
		else
		{
			return "Range " + start + "-" + end;
		}
	}
}
