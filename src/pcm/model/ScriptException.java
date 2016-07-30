package pcm.model;

public class ScriptException extends Exception {
    private static final long serialVersionUID = 1L;
    public Script script = null;

    public ScriptException() {
        super();
    }

    public ScriptException(String message, Throwable cause, Script script) {
        super(message, cause);
        this.script = script;
    }

    public ScriptException(String message, ScriptException cause) {
        super(message, cause);
        this.script = cause.script;
    }

    ScriptException(String message, Script script) {
        super(message);
        this.script = script;
    }
}
