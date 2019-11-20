package pcm.model;

/**
 * @author Citizen-Cane
 * 
 */
public class ScriptExecutionException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptExecutionException(Script script, Action action, Exception e) {
        super(script, formatMessage(script, action, e.getMessage()), e);
    }

    public ScriptExecutionException(Script script, Action action, String message) {
        super(script, formatMessage(script, action, message));
    }

}
