package pcm.state;

import pcm.model.AbstractAction.Statement;
import pcm.model.ConditionRange;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;

public class BasicCondition implements Condition {
    protected final ParameterizedStatement statement;

    protected static abstract class ParameterizedStatement {
        private final Statement statement;
        private final CommandLineParameters<?> args;

        public <T extends Enum<?>> ParameterizedStatement(Statement statement, CommandLineParameters<T> args) {
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

}
