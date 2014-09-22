package pcm.model;

public class ScriptError extends Exception {
	private static final long serialVersionUID = 1L;
	public Script script = null;

	public ScriptError() {
		super();
	}

	public ScriptError(String message, Throwable cause, Script script) {
		super(message, cause);
		this.script = script;
	}

	public ScriptError(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptError(String message) {
		super(message);
	}

	public ScriptError(Throwable cause) {
		super(cause);
	}

}
