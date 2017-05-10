package pcm.state;

import java.util.Arrays;

import pcm.model.AbstractAction.Statement;
import pcm.state.persistence.ScriptState;

public class BasicCondition implements Condition {
    protected final ParameterizedStatement statement;

    protected static abstract class ParameterizedStatement {
        private final Statement statement;
        private final String[] args;

        public ParameterizedStatement(Statement statement, String[] args) {
            super();
            this.statement = statement;
            this.args = Arrays.copyOf(args, 1);
        }

        protected abstract boolean isTrueFor(ScriptState state);

        @Override
        public String toString() {
            return statement.toString() + args.toString();
        }
    }

    public BasicCondition(ParameterizedStatement statement) {
        this.statement = statement;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        return statement.isTrueFor(state);
    }

}
