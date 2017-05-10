package pcm.state;

import java.util.Arrays;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;

public class BasicCommand implements Command {
    protected final ParameterizedStatement statement;

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

    public BasicCommand(ParameterizedStatement statement) {
        this.statement = statement;
    }

    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        statement.run(state);
    }

}
