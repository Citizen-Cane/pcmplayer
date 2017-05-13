package pcm.state.conditions;

import pcm.controller.StateCommandLineParameters;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.persistence.ScriptState;

public class StateCondition extends BasicCondition {
    private static final Statement STATE = AbstractAction.Statement.State;

    public StateCondition(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
            final String[] items = args.items();
            if (args.containsKey(StateCommandLineParameters.Keyword.Is)) {
                final Object[] attributes = args.array(StateCommandLineParameters.Keyword.Is);
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
            } else {
                throw new IllegalStatementException(STATE, args);
            }
        } catch (

        ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

}
