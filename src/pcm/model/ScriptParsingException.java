package pcm.model;

public class ScriptParsingException extends ScriptException {
    private static final long serialVersionUID = 1L;

    private static final String DefaultReason = "Unexpected statement";

    public ScriptParsingException(int lineNumber, int actionNumber, String line,
            Throwable t, Script script) {
        super(actionNumber > 0 ? DefaultReason + " in line " + lineNumber
                + ", Action " + actionNumber + ": " + line : DefaultReason
                + " in line " + lineNumber + ": " + line, t, script);
    }

    public ScriptParsingException(int lineNumber, int actionNumber, String line,
            String reason, Script script) {
        super(actionNumber > 0 ? reason + " in line " + lineNumber
                + ", Action " + actionNumber + ": " + line : reason
                + " in line " + lineNumber + ": " + line, script);
    }
}
