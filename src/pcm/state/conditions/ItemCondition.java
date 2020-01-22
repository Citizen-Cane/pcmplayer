package pcm.state.conditions;

import static pcm.state.StateCommandLineParameters.Keyword.Applied;
import static pcm.state.StateCommandLineParameters.Keyword.To;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pcm.controller.Declarations;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCondition;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.commands.ItemCommand;
import pcm.state.persistence.ScriptState;
import teaselib.util.DurationFormat;
import teaselib.util.Item;

public class ItemCondition extends BasicCondition {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCondition(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedStatement statement(StateCommandLineParameters args) {
        final ParameterizedStatement innerStatement = innerStatement(args);
        if (args.containsKey(Keyword.Not)) {
            return new ParameterizedStatement(ITEM, args) {
                @Override
                public boolean call(ScriptState state) {
                    return !innerStatement.call(state);
                }
            };
        } else {
            return innerStatement;
        }
    }

    static ParameterizedStatement innerStatement(StateCommandLineParameters args) {
        String[] items = args.items(Keyword.Item);
        Declarations declarations = args.getDeclarations();
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
        } else {
            throw new IllegalStatementException("Keyword not found", args);
        }
    }

    private static ParameterizedStatement is(StateCommandLineParameters args, String[] items) {
        Object[] attributes = args.items(StateCommandLineParameters.Keyword.Is);
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.item(item).is(attributes)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement available(StateCommandLineParameters args, String[] items) {
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.item(item).isAvailable()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement canApply(StateCommandLineParameters args, String[] items) {
        String[] peers = args.items(Keyword.To);
        return new ParameterizedStatement(ITEM, args) {
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

    private static ParameterizedStatement applied(StateCommandLineParameters args, String[] items) {
        String[] peers = BasicCondition.optionalPeers(args, Applied, To);
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (peers.length == 0) {
                        if (!state.player.item(item).applied()) {
                            return false;
                        }
                    } else {
                        for (String peer : peers) {
                            if (!state.player.item(item).is(peer)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement free(StateCommandLineParameters args, String[] items) {
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (state.player.item(item).applied()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement expired(StateCommandLineParameters args, String[] items) {
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.item(item).expired()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement remaining(StateCommandLineParameters args, String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.item(item).duration().remaining(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement elapsed(StateCommandLineParameters args, String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.item(item).duration().elapsed(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement limit(StateCommandLineParameters args, String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.item(item).duration().limit(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement matching(StateCommandLineParameters args, String[] items) {
        String[] attributes = args.items(StateCommandLineParameters.Keyword.Matching);
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public boolean call(ScriptState state) {
                ItemCommand.replaceWithMatching(args, items, attributes, state);
                return innerStatement(args).call(state);
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
