package pcm.model;

public class ValidationIssue extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ValidationIssue(Action action, String reason, Script script) {
        super("Action " + action.number + ": " + reason, script);
    }

    public ValidationIssue(Action action, ScriptParsingException e) {
        super("Action " + action.number + ": " + e.getMessage(), e.getCause(),
                e.script);
    }

    public ValidationIssue(String reason, Throwable e, Script script) {
        super(reason, e, script);
    }

    public ValidationIssue(String reason, Script script) {
        super(reason, script);
    }

    public ValidationIssue(Action action, Throwable e, Script script) {
        super("Action " + action.number + ": ", e, script);
    }
}
