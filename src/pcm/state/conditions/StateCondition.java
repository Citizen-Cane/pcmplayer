package pcm.state.conditions;

import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.util.DurationFormat;

public class StateCondition extends BasicCondition {
    private static final Statement STATE = AbstractAction.Statement.State;
    private final StateCommandLineParameters args;

    public StateCondition(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
            ParameterizedStatement innerStatement = innerStatement(args);
            if (args.containsKey(Keyword.Not)) {
                return new ParameterizedStatement(STATE, args) {
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
        String[] items = args.items(Keyword.Item);
        if (args.containsKey(StateCommandLineParameters.Keyword.Is)) {
            return is(args, items);
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

    private static ParameterizedStatement is(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        Object[] attributes = args.items(StateCommandLineParameters.Keyword.Is);
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.state(item).is(attributes)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement applied(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        String[] peers = args.items(Keyword.To);
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (peers.length == 0) {
                        if (!state.player.state(item).applied()) {
                            return false;
                        }
                    } else {
                        for (String peer : peers) {
                            if (!state.player.state(item).is(peer)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement free(final StateCommandLineParameters args, final String[] items) {
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (state.player.state(item).applied()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement expired(final StateCommandLineParameters args, final String[] items) {
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!state.player.state(item).expired()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement remaining(final StateCommandLineParameters args, final String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.state(item).duration().remaining(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement elapsed(final StateCommandLineParameters args, final String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.state(item).duration().elapsed(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement limit(final StateCommandLineParameters args, final String[] items) {
        Keyword condition = args.getCondition();
        StateCommandLineParameters.Operator op = args.getOperator(condition);
        DurationFormat durationFormat = new DurationFormat(args.value(condition));
        return new ParameterizedStatement(STATE, args) {
            @Override
            public boolean call(ScriptState state) {
                for (String item : items) {
                    if (!(op.isTrueFor(state.player.state(item).duration().limit(TimeUnit.SECONDS),
                            durationFormat.toSeconds()))) {
                        return false;
                    }
                }
                return true;
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
