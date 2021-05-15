package pcm.model;

/**
 * @author Citizen-Cane
 * 
 */
public class ScriptExecutionException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public final Action action;

    public ScriptExecutionException(Script script, Action action, Exception e) {
        super(script, formatMessage(script, action, e.getMessage()), e);
        this.action = action;
    }

    public ScriptExecutionException(Script script, Action action, String message) {
        super(script, formatMessage(script, action, message));
        this.action = action;
    }

}
