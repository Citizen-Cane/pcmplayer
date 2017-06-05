package pcm.model;

import pcm.model.AbstractAction.Statement;
import teaselib.core.util.CommandLineParameters;

public class IllegalStatementException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public IllegalStatementException(Statement statement, CommandLineParameters<?> args) {
        super("Illegal statement arguments ." + statement + " " + args.toString());
    }

    public IllegalStatementException(Statement statement, String args) {
        super("Illegal statement arguments ." + statement + " " + args);
    }
}
