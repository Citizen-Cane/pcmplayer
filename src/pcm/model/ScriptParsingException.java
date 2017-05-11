package pcm.model;

public class ScriptParsingException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptParsingException(int lineNumber, int actionNumber, String line, Throwable t, Script script) {
        super(actionNumber > 0 ? reason(t) + " in line " + lineNumber + ", Action " + actionNumber + ": " + line
                : reason(t) + " in line " + lineNumber + ": " + line, t, script);
    }

    public ScriptParsingException(int lineNumber, int actionNumber, String line, String reason, Script script) {
        super(actionNumber > 0 ? reason + " in line " + lineNumber + ", Action " + actionNumber + ": " + line
                : reason + " in line " + lineNumber + ": " + line, script);
    }

    private static String reason(Throwable t) {
        String message = t.getClass().getName() + ": " + t.getMessage();
        return message;
    }

    public ScriptParsingException(Exception e) {
        super(e.getMessage(), e);
    }
}
