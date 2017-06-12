package pcm.state.commands;

import pcm.controller.Player;
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
import teaselib.core.StateMaps;
import teaselib.core.StateMaps.Attributes;

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
                final String[] peers = args.items(Keyword.To);
                if (args.containsKey(Keyword.To) && peers.length == 0) {
                    throw new IllegalArgumentException("Missing peers to apply the item to");
                } else if (args.containsKey(Keyword.Apply) && args.items(Keyword.Apply).length > 0) {
                    throw new IllegalArgumentException(
                            "Apply just applies the default peers - use 'To' to apply additional peers");
                }
                final DurationFormat duration = args.durationOption();
                final boolean remember = args.rememberOption();
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        Player player = state.player;
                        for (String item : items) {
                            Attributes attributeApplier = (StateMaps.Attributes) player.item(item);
                            attributeApplier.applyAttributes(player.script.scriptApplyAttribute);
                            attributeApplier.applyAttributes(player.namespaceApplyAttribute);
                            State.Options options = player.item(item).to(peers);
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
            } else if (args.containsKey(Keyword.SetAvailable)) {
                final boolean setAvailable = Boolean.parseBoolean(args.value(Keyword.SetAvailable));
                return new ParameterizedStatement(ITEM, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (String item : items) {
                            state.player.item(item).setAvailable(setAvailable);
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
