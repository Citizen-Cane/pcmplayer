package pcm.model;

public class ValidationError extends ScriptError {
	private static final long serialVersionUID = 1L;

	public ValidationError(String reason)
	{
		super(reason);
	}

	public ValidationError(Action action, String reason)
	{
		super("Action " + action.number + ": " + reason);
	}

	public ValidationError(String reason, Throwable e)
	{
		super(reason, e);
	}

	public ValidationError(Action action, Throwable e)
	{
		super("Action " + action.number, e);
	}

	public ValidationError(Action action, String reason, Throwable e)
	{
		super("Action " + action.number + ": " + reason, e);
	}
}
