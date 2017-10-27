package pcm.state.conditions;

import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;

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
            final ParameterizedStatement innerStatement = innerStatement(args);
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
        final String[] items = args.items(Keyword.Item);
        if (args.containsKey(StateCommandLineParameters.Keyword.Is)) {
            final Object[] attributes = args.items(StateCommandLineParameters.Keyword.Is);
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Applied)) {
            return new ParameterizedStatement(STATE, args) {

                @Override
                public boolean call(ScriptState state) {
                    for (String item : items) {
                        if (!state.player.state(item).applied()) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Free)) {
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Expired)) {
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Remaining)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Elapsed)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
        } else if (args.containsKey(StateCommandLineParameters.Keyword.Limit)) {
            Keyword condition = args.getCondition();
            final StateCommandLineParameters.Operator op = args.getOperator(condition);
            final DurationFormat durationFormat = new DurationFormat(args.value(condition));
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
