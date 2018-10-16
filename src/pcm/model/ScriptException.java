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

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
        this.script = null;
    }

    ScriptException(String message, Script script) {
        super(message);
        this.script = script;
    }

    @Override
    public String getMessage() {
        return (script != null ? script.name + ": " : "") + super.getMessage();
    }
}
