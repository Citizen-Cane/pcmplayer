package pcm.state;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;

public class BasicCommand implements Command {
    protected final ParameterizedStatement statement;

    protected abstract static class ParameterizedStatement {
        private final Statement statement;
        private final CommandLineParameters<?> args;

        public <T extends Enum<T>> ParameterizedStatement(Statement statement, CommandLineParameters<T> args) {
            this.statement = statement;
            this.args = args;
        }

        public abstract void run(ScriptState state) throws ScriptExecutionException;

        @Override
        public String toString() {
            return statement.toString() + " " + args.toString();
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
