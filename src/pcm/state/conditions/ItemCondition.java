package pcm.state.conditions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCondition;
import pcm.state.ParameterizedConditionStatement;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.util.Item;

public class ItemCondition extends BasicCondition {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCondition(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedConditionStatement statement(StateCommandLineParameters args) {
        final ParameterizedConditionStatement innerStatement = innerStatement(args);
        if (args.containsKey(Keyword.Not)) {
            return new ParameterizedConditionStatement(ITEM, args) {
                @Override
                public boolean call(ScriptState state) {
                    return !innerStatement.call(state);
                }
            };
        } else {
            return innerStatement;
        }
    }

    static ParameterizedConditionStatement innerStatement(StateCommandLineParameters args) {
        String[] items = args.items(Keyword.Item);
        var declarations = args.getDeclarations();
        declarations.validate(items, Item.class);
        if (args.containsKey(StateCommandLineParameters.Keyword.Matching)) {
            return matching(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Is)) {
            return is(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Available)) {
            return available(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.CanApply)) {
            return canApply(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Applied)) {
            return applied(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Free)) {
            return free(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Expired)) {
            return expired(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Remaining)) {
            return remaining(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Elapsed)) {
            return elapsed(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Limit)) {
            return limit(args, items);
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Removed)) {
            return removed(args, items);
        } else {
            throw new IllegalStatementException("Keyword not found", args);
        }
    }

    private static ParameterizedConditionStatement is(StateCommandLineParameters args, String[] items) {
        Object[] attributes = args.items(StateCommandLineParameters.Keyword.Is);
        return new ParameterizedConditionStatement.Boolean(args, items, ITEM,
                (player, value) -> player.item(value).is(attributes));
    }

    private static ParameterizedConditionStatement available(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Boolean(args, items, ITEM,
                (player, value) -> player.item(value).isAvailable());
    }

    private static ParameterizedConditionStatement canApply(StateCommandLineParameters args, String[] items) {
        String[] peers = args.items(Keyword.To);
        return new ParameterizedConditionStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.item(item).canApply()) {
                        return false;
                    }
                    for (String peer : peers) {
                        if (state.player.state(peer).applied()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedConditionStatement applied(StateCommandLineParameters args, String[] items) {
        return applied(args, items, ITEM, (player, value) -> player.item(value));
    }

    private static ParameterizedConditionStatement free(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Boolean(args, items, ITEM,
                (player, value) -> player.item(value).removed());
    }

    private static ParameterizedConditionStatement expired(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Boolean(args, items, ITEM,
                (player, value) -> player.item(value).expired());
    }

    private static ParameterizedConditionStatement remaining(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, ITEM,
                (player, value) -> player.item(value).duration().remaining(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement elapsed(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, ITEM,
                (player, value) -> player.item(value).duration().elapsed(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement limit(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, ITEM,
                (player, value) -> player.item(value).duration().limit(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement removed(StateCommandLineParameters args, String[] items) {
        return new ParameterizedConditionStatement.Duration(args, items, ITEM,
                (player, value) -> player.item(value).removed(TimeUnit.SECONDS));
    }

    private static ParameterizedConditionStatement matching(StateCommandLineParameters args, String[] items) {
        String[] attributes = args.items(StateCommandLineParameters.Keyword.Matching);
        return new ParameterizedConditionStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                ((StateCommandLineParameters) args).replaceWithMatching(items, attributes, state);
                return innerStatement((StateCommandLineParameters) args).call(state);
            }
        };
    }

    @Override
    public String toString() {
        return args.toString();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
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
        ItemCondition other = (ItemCondition) obj;
        if (args == null) {
            if (other.args != null)
                return false;
        } else if (!args.equals(other.args))
            return false;
        return true;
    }

    public List<String> items() {
        return Arrays.asList(args.items(Keyword.Item));
    }
}
