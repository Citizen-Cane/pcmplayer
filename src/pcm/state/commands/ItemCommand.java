package pcm.state.commands;

import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
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
    private final StateCommandLineParameters args;

    public ItemCommand(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
            final String[] items = args.items(Keyword.Item);
            if (args.containsKey(Keyword.Apply)) {
                final String[] attributes = args.items(Keyword.To);
                final DurationFormat duration = args.durationOption();
                final boolean remember = args.rememberOption();
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (String item : items) {
                            State.Options options;
                            options = state.player.item(item).to(attributes);
                            args.handleStateOptions(options, duration, remember);
                        }
                    }

                };
            } else if (args.containsKey(Keyword.Remove)) {
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (String item : items) {
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

    @Override
    public String toString() {
        return args.toString();
    }

}
