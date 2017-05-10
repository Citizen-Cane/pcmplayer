package pcm.model;

import java.util.Arrays;

import pcm.model.AbstractAction.Statement;

public class IllegalStatementException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public IllegalStatementException(Statement statement, String[] args) {
        super("Illegal statement arguments ." + statement + Arrays.asList(args).toString());
    }
}
