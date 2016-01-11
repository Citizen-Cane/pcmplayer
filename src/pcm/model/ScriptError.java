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

    public ScriptError(String message, ScriptError cause) {
        super(message, cause);
        this.script = cause.script;
    }

    ScriptError(String message, Script script) {
        super(message);
        this.script = script;
    }
}
