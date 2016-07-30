package pcm.model;

public class ScriptExecutionException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptExecutionException(String reason) {
        super(reason, (Script) null);
    }

    public ScriptExecutionException(String reason, Script script) {
        super(reason + " in script " + script.name, script);
    }

    public ScriptExecutionException(String reason, Throwable e, Script script) {
        super(reason, e, script);
    }

    public ScriptExecutionException(Action action, String reason, Throwable e,
            Script script) {
        super(reason + " in Action " + action.number, e, script);
    }
}
