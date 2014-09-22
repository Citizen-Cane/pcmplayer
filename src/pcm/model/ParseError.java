package pcm.model;

public class ParseError extends ScriptError
{
	private static final long serialVersionUID = 1L;

	public ParseError(int lineNumber, int actionNumber, String line, Throwable t)
	{
		this(lineNumber, actionNumber, line, t, "Unexpected statement");
	}

	public ParseError(int lineNumber, int actionNumber, String line, Throwable t, Script script)
	{
		this(lineNumber, actionNumber, line, t, "Unexpected statement");
		this.script = script;
	}

	public ParseError(int lineNumber, int actionNumber, String line, String reason, Script script)
	{
		this(lineNumber, actionNumber, line, null, reason);
		this.script = script;
	}

	public ParseError(int lineNumber, int actionNumber, String line, String reason)
	{
		this(lineNumber, actionNumber, line, null, reason);
	}

	public ParseError(int lineNumber, int actionNumber, String line, Throwable t, String reason)
	{
		super(actionNumber > 0
				? reason + " in line " + lineNumber + ", Action " + actionNumber + ": " + line
				: reason + " in line " + lineNumber + ": " + line
				, t);
	}
}
