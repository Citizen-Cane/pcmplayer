package pcm.model;

public class ScriptExecutionError extends ScriptError {
    private static final long serialVersionUID = 1L;

    public ScriptExecutionError(String reason) {
        super(reason, (Script) null);
    }

    public ScriptExecutionError(String reason, Script script) {
        super(reason + " in script " + script.name, script);
    }

    public ScriptExecutionError(String reason, Throwable e, Script script) {
        super(reason, e, script);
    }

    public ScriptExecutionError(Action action, String reason, Throwable e,
            Script script) {
        super(reason + " in Action " + action.number, e, script);
    }
}
