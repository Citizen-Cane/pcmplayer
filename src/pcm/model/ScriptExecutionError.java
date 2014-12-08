package pcm.model;

public class ScriptExecutionError extends ScriptError {
    private static final long serialVersionUID = 1L;

    public ScriptExecutionError(String reason) {
        super(reason);
    }

    public ScriptExecutionError(Action action, String reason) {
        super(reason + " in Action " + action.number);
    }

    public ScriptExecutionError(String reason, Throwable e) {
        super(reason, e);
    }

    public ScriptExecutionError(Script script, String reason) {
        super(reason + " in script " + script.name);
    }

    public ScriptExecutionError(Script script, String reason, Throwable e) {
        super(reason, e);
    }

    public ScriptExecutionError(Action action, String reason, Throwable e) {
        super(reason + " in Action " + action.number, e);
    }

    public ScriptExecutionError(Action action, String reason, Throwable e,
            Script script) {
        super(reason + " in Action " + action.number, e, script);
    }
}
