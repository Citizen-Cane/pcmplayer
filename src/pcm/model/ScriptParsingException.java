package pcm.model;

public class ScriptParsingException extends ScriptException {
    private static final long serialVersionUID = 1L;

    final int lineNumber;
    final String line;

    public ScriptParsingException(Script script, Throwable t, int lineNumber, String line) {
        super(script, formatMessage(script, t.getMessage(), lineNumber), t);
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public ScriptParsingException(Script script, String message, int lineNumber, String line) {
        super(script, formatMessage(script, message, lineNumber));
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public ScriptParsingException(Script script, Action action, String message, int lineNumber, String line) {
        super(script, formatMessage(script, action, message, lineNumber));
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public ScriptParsingException(Script script, Action action, Throwable t, int lineNumber, String line) {
        super(script, formatMessage(script, action, t.getMessage(), lineNumber), t);
        this.lineNumber = lineNumber;
        this.line = line;
    }

}
