package pcm.model;

public class ScriptException extends Exception {
    private static final long serialVersionUID = 1L;

    public final Script script;

    ScriptException(Script script, String message) {
        super(message);
        this.script = script;
    }

    public ScriptException(Script script, String message, Throwable t) {
        super(message, t);
        this.script = script;
    }

    static String formatMessage(Script script, String message) {
        return String.format("%1$s: %2$s", script.name, message);
    }

    static String formatMessage(Script script, String message, int lineNumber) {
        return String.format("%1$s:%3$s: %2$s", script, message, lineNumber);
    }

    static String formatMessage(Script script, Action action, String message) {
        return String.format("%1$s, action %2$s: %3$s", script, action, message);
    }

    static String formatMessage(Script script, Action action, String message, int lineNumber) {
        return String.format("%1$s:%4$s, action %2$s: %3$s", script, action, message, lineNumber);
    }

}
