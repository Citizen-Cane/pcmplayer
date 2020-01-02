package pcm.state;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.StateCommandLineParameters.Keyword;
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

    protected static Object[] optionalPeers(StateCommandLineParameters args, Keyword condition, Keyword peerList) {
        Object[] peers = args.items(args.containsKey(peerList) ? peerList : condition);
        if (args.containsKey(peerList) && peers.length == 0) {
            throw new IllegalArgumentException("Missing peers to " + condition.name().toLowerCase() + " the item '"
                    + peerList.name().toLowerCase() + "'");
        } else if (args.containsKey(condition) && args.items(condition).length > 0) {
            throw new IllegalArgumentException("'" + condition.name() + "' just applies the default peers - use '"
                    + condition.name() + " " + peerList.name() + "' to apply additional peers");
        }
        return peers;
    }

}
