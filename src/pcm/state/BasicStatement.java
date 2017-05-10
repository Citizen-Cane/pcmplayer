package pcm.state;

import java.util.Arrays;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;

public class BasicStatement {
    protected final ParameterizedStatement statement;

    public static class IllegalStatementException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public IllegalStatementException(Statement statement, String[] args) {
            super("Illegal statement arguments ." + statement + Arrays.asList(args).toString());
        }
    }

    protected static abstract class ParameterizedStatement {
        private final Statement statement;
        private final String[] args;

        public ParameterizedStatement(Statement statement, String[] args) {
            super();
            this.statement = statement;
            this.args = Arrays.copyOf(args, 1);
        }

        protected abstract void run(ScriptState state) throws ScriptExecutionException;

        @Override
        public String toString() {
            return statement.toString() + args.toString();
        }
    }

    public BasicStatement(ParameterizedStatement statement) {
        this.statement = statement;
    }

    protected void runStatement(ScriptState state) throws ScriptExecutionException {
        statement.run(state);
    }

    public void execute(ScriptState state) throws ScriptExecutionException {
        statement.run(state);
    }

}
