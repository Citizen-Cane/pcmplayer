package pcm.model;

public class ScriptParsingException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptParsingException(int lineNumber, int actionNumber, String line, String reason, Script script) {
        this(lineNumber, actionNumber, line, reason, null, script);
    }

    public ScriptParsingException(int lineNumber, int actionNumber, String line, Throwable t, Script script) {
        this(lineNumber, actionNumber, line, t.getMessage(), t, script);
    }

    ScriptParsingException(int lineNumber, int actionNumber, String line, String reason, Throwable cause,
            Script script) {
        super(reason + " in line " + lineNumber + (actionNumber > 0 ? ", Action " + actionNumber : "") + ": " + line,
                cause, script);
    }

    public ScriptParsingException(Exception e) {
        super(e.getMessage(), e);
    }
}
