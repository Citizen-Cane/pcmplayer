package pcm.state.conditions;

import java.util.concurrent.TimeUnit;

import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.persistence.ScriptState;

public class ItemCondition extends BasicCondition {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCondition(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
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
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

    private static ParameterizedStatement innerStatement(final StateCommandLineParameters args)
            throws ClassNotFoundException {
        final String[] items = args.items(Keyword.Item);
        if (args.containsKey(StateCommandLineParameters.Keyword.Is)) {
            final Object[] attributes = args.items(StateCommandLineParameters.Keyword.Is);
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Available)) {
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.CanApply)) {
            final String[] peers = args.items(Keyword.To);
            return new ParameterizedStatement(ITEM, args) {
                @Override
                public boolean call(ScriptState state) {
                    for (String item : items) {
                        if (!state.player.item(item).canApply()) {
                            return false;
                        }
                        for (String peer : peers) {
                            if (state.player.item(peer).applied()) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            };
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Applied)) {
            final String[] peers = args.items(Keyword.To);
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Free)) {
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Expired)) {
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Remaining)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Elapsed)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Limit)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
        } else {
            throw new IllegalStatementException("Keyword not found", args);
        }
    }

    @Override
    public String toString() {
        return args.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
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

}
