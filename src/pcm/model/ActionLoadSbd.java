package pcm.model;

public class ActionLoadSbd extends ActionRange {
	public final Script script;
	
	public ActionLoadSbd(Script script, int start, int end)
	{
		super(start, end);
		this.script = script;
	}
}
