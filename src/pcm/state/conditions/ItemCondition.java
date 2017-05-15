package pcm.state.conditions;

import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
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
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public boolean call(ScriptState state) {
                        for (String item : items) {
                            if (!state.player.item(item).canApply()) {
                                return false;
                            }
                        }
                        return true;
                    }
                };
            } else if (args.containsKey(StateCommandLineParameters.Keyword.Applied)) {
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public boolean call(ScriptState state) {
                        for (String item : items) {
                            if (!state.player.item(item).applied()) {
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
            } else {
                throw new IllegalStatementException(ITEM, args);
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
