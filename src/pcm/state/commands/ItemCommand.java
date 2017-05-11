package pcm.state.commands;

import pcm.controller.StateCommandLineParameters;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class ItemCommand extends BasicCommand {
    private static final Statement ITEM = AbstractAction.Statement.Item;

    public ItemCommand(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
            final Enum<?>[] items = args.items();
            if (args.containsKey(StateCommandLineParameters.Keyword.Apply)) {
                final Object[] attributes = args.options(StateCommandLineParameters.Keyword.To);
                final DurationFormat duration = args.durationOption();
                final boolean remember = args.rememberOption();
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            State.Options options;
                            options = state.player.item(item).to(attributes);
                            args.handleStateOptions(options, duration, remember);
                        }
                    }

                };
            } else if (args.containsKey(StateCommandLineParameters.Keyword.Remove)) {
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            state.player.item(item).remove();
                        }
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

}
