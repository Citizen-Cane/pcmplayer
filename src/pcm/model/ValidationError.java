package pcm.model;

public class ValidationError extends ScriptError {
    private static final long serialVersionUID = 1L;

    public ValidationError(Action action, String reason, Script script) {
        super("Action " + action.number + ": " + reason, script);
    }

    public ValidationError(Action action, ParseError e) {
        super("Action " + action.number + ": " + e.getMessage(), e.getCause(),
                e.script);
    }

    public ValidationError(String reason, Throwable e, Script script) {
        super(reason, e, script);
    }

    public ValidationError(String reason, Script script) {
        super(reason, script);
    }

    public ValidationError(Action action, Throwable e, Script script) {
        super("Action " + action.number + ": ", e, script);
    }
}
