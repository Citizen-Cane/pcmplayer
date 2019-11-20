package pcm.model;

public class ValidationIssue extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ValidationIssue(Script script, Action action, String message) {
        super(script, formatMessage(script, action, message));
    }

    public ValidationIssue(Script script, String message) {
        super(script, formatMessage(script, message));
    }

    public ValidationIssue(Script script, Action action, Exception e) {
        super(script, formatMessage(script, action, e.getMessage()), e);
    }
}
