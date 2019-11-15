package pcm.model;

/**
 * @author Citizen-Cane
 * 
 */
public class ScriptExecutionException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptExecutionException(String reason) {
        super(reason, (Script) null);
    }

    public ScriptExecutionException(String reason, Script script) {
        super(reason + " in script " + script.name, script);
    }

    public ScriptExecutionException(Action action, Throwable e, Script script) {
        super(action + ": " + e.getMessage(), e, script);
    }

    public ScriptExecutionException(String message, Throwable e, Script script) {
        super(message + ": " + e.getMessage(), e, script);
    }

    public ScriptExecutionException(Action action, String reason, Script script) {
        super(reason + " in " + action, script);
    }

    public ScriptExecutionException(Action action, String reason, Throwable e, Script script) {
        super(reason + " in " + action + ": " + e.getMessage(), e, script);
    }

}
