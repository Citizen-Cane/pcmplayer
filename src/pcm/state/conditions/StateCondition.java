package pcm.state.conditions;

import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCondition;
import pcm.state.StateKeywords;
import pcm.state.ParameterizedConditionStatement;
import pcm.state.StateCommandLineParameters;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class StateCondition extends BasicCondition {
    private static final Statement STATE = AbstractAction.Statement.State;
    private final StateCommandLineParameters args;

    public StateCondition(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedConditionStatement statement(final StateCommandLineParameters args) {
        ParameterizedConditionStatement innerStatement = innerStatement(args);
        if (args.containsKey(StateKeywords.Not)) {
            return new ParameterizedConditionStatement(STATE, args) {
                @Override
                public boolean call(ScriptState state) {
                    return !innerStatement.call(state);
                }
            };
        } else {
            return innerStatement;
        }
    }

    private static ParameterizedConditionStatement innerStatement(final StateCommandLineParameters args) {
        String[] items = args.items(StateKeywords.Item);
        args.getDeclarations().validate(items, State.class);

        if (args.containsKey(StateKeywords.Is)) {
            return is(args, items);
        } else if (args.containsKey(StateKeywords.Applied)) {
            return applied(args, items);
        } else if (args.containsKey(StateKeywords.Free)) {
            return free(args, items);
        } else if (args.containsKey(StateKeywords.Expired)) {
            return expired(args, items);
        } else if (args.containsKey(StateKeywords.Remaining)) {
            return remaining(args, items);
        } else if (args.containsKey(StateKeywords.Elapsed)) {
            return elapsed(args, items);
        } else if (args.containsKey(StateKeywords.Limit)) {
            return limit(args, items);
        } else if (args.containsKey(StateKeywords.Removed)) {
            return removed(args, items);
        } else {
            throw new IllegalStatementException("State condition not found or invalid", args);
        }
    }

    private static ParameterizedConditionStatement is(StateCommandLineParameters args, String[] items) {
        Object[] attributes = args.items(StateKeywords.Is);
        return new ParameterizedConditionStatement.Boolean(args, items, STATE,
                (player, value) -> player.state(value).is(attributes));
    }

    private static ParameterizedConditionStatement applied(StateCommandLineParameters args, String[] items) {
        return applied(args, items, STATE, (player, value) -> player.state(value));
    }

    private static ParameterizedConditionStatement free(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Boolean(args, items, STATE,
                (player, value) -> player.state(value).removed());
    }

    private static ParameterizedConditionStatement expired(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Boolean(args, items, STATE,
                (player, value) -> player.state(value).expired());
    }

    private static ParameterizedConditionStatement remaining(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, STATE,
                (player, value) -> player.state(value).duration().remaining(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement elapsed(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, STATE,
                (player, value) -> player.state(value).duration().elapsed(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement limit(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, STATE,
                (player, value) -> player.state(value).duration().limit(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement removed(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, STATE,
                (player, value) -> player.state(value).removed(TimeUnit.SECONDS));
    }

    @Override
    public String toString() {
        return args.toString();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((args == null) ? 0 : args.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StateCondition other = (StateCondition) obj;
        if (args == null) {
            if (other.args != null)
                return false;
        } else if (!args.equals(other.args))
            return false;
        return true;
    }
}
