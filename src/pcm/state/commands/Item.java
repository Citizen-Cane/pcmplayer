package pcm.state.commands;

import java.util.List;

import pcm.controller.StateCommandLineParameters;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class Item extends BasicCommand {
    public Item(String[] args) throws ScriptParsingException {
        super(statement(AbstractAction.Statement.Item, args));
    }

    private static ParameterizedStatement statement(Statement statement, String[] args) throws ScriptParsingException {
        final StateCommandLineParameters cmd = new StateCommandLineParameters(args);
        try {
            if (cmd.containsKey(StateCommandLineParameters.Keyword.Apply)) {
                final List<Enum<?>> items = cmd.applyOptions();
                final Object[] attributes = cmd.toOptions();
                final DurationFormat duration = cmd.durationOption();
                final boolean remember = cmd.rememberOption();
                return new ParameterizedStatement(statement, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            State.Options options;
                            if (attributes.length > 0) {
                                options = state.player.item(item).to(attributes);
                            } else {
                                options = state.player.item(item).to(attributes);
                            }
                            cmd.handleStateOptions(options, duration, remember);
                        }
                    }

                };
            } else if (cmd.containsKey(StateCommandLineParameters.Keyword.Remove)) {
                final List<Enum<?>> items = cmd.removeOptions();
                return new ParameterizedStatement(statement, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            state.player.item(item).remove();
                        }
                    }
                };
            } else {
                throw new IllegalStatementException(statement, args);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

}
