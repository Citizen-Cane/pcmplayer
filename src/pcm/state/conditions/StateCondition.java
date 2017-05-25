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
            } else {
                throw new IllegalStatementException(STATE, args);
            }
        } catch (

        ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

    @Override
    public String toString() {
        return args.toString();
    }

}
