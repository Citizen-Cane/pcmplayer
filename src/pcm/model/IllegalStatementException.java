package pcm.model;

import pcm.model.AbstractAction.Statement;
import teaselib.core.util.CommandLineParameters;

public class IllegalStatementException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public IllegalStatementException(String message, CommandLineParameters<?> args) {
        super(message + ": " + args.toString());
    }

    public IllegalStatementException(Statement statement, String args) {
        super("Illegal statement arguments ." + statement + " " + args);
    }
}
