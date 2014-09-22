package pcm.model;

public class AskItem {
	public final int n;
	public final int action;
	public final String message;
	
	AskItem(int n, String message)
	{
		this.n = n;
		this.action = 0;
		this.message = message;
	}

	AskItem(int n, int action, String message)
	{
		this.n = n;
		this.action = action;
		this.message = message;
	}
}
