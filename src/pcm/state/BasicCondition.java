package pcm.state;

import pcm.model.AbstractAction.Statement;
import pcm.model.ConditionRange;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;

public class BasicCondition implements Condition {
    protected final ParameterizedStatement statement;

    protected abstract static class ParameterizedStatement {
        private final Statement statement;
        private final CommandLineParameters<?> args;

        public <T extends Enum<T>> ParameterizedStatement(Statement statement, CommandLineParameters<T> args) {
            this.statement = statement;
            this.args = args;
        }

        public abstract boolean call(ScriptState state);

        @Override
        public String toString() {
            return statement.toString() + " " + args.toString();
        }
    }

    public BasicCondition(ParameterizedStatement statement) {
        this.statement = statement;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        return statement.call(state);
    }

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        return conditionRange.contains(this);
    }

    protected static String[] optionalPeers(StateCommandLineParameters args, Keyword condition, Keyword peerList) {
        String[] peers = args.items(args.containsKey(peerList) ? peerList : condition);
        if (args.containsKey(peerList) && peers.length == 0) {
            throw new IllegalArgumentException("Missing peers");
        } else if (args.containsKey(condition) && args.items(condition).length > 0) {
            throw new IllegalArgumentException("'" + condition.name() + "' just applies to the default peers - use '"
                    + condition.name() + " " + peerList.name() + "' to apply additional peers");
        }
        return peers;
    }

}
