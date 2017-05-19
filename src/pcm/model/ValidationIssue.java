package pcm.model;

public class ValidationIssue extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ValidationIssue(Action action, String reason, Script script) {
        super("Action " + action.number + ": " + reason, script);
    }

    public ValidationIssue(Action action, ScriptParsingException e) {
        super("Action " + action.number + ": " + e.getMessage(), e.getCause(), e.script);
    }

    public ValidationIssue(Action action, Exception e) {
        super("Action " + action.number + ": " + e.getMessage(), e.getCause());
    }

    public ValidationIssue(String reason, Script script) {
        super(reason, script);
    }

    public ValidationIssue(Action action, Exception e, Script script) {
        super("Action " + action.number + ": " + e.getMessage(), e, script);
    }
}
