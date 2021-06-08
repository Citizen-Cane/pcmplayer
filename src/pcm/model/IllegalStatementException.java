package pcm.model;

import pcm.model.AbstractAction.Statement;
import teaselib.core.util.CommandLineParameters;

public class IllegalStatementException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public IllegalStatementException(String type, Object value, CommandLineParameters<?> args) {
        super("Unexpected " + type + " " + value + " in " + args);
    }

    public IllegalStatementException(String message, CommandLineParameters<?> args) {
        super(message + ": " + args);
    }

    public IllegalStatementException(Statement statement, String args) {
        super("Unexpected statement " + statement + " in " + args);
    }

}
