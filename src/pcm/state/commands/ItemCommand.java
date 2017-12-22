package pcm.state.commands;

import pcm.controller.Player;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.core.StateMaps;
import teaselib.core.StateMaps.Attributes;
import teaselib.util.Item;

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
            String[] items = args.items(Keyword.Item);
            if (args.containsKey(Keyword.Apply)) {
                return apply(args, items);
            } else if (args.containsKey(Keyword.Remove)) {
                return remove(args, items);
            } else if (args.containsKey(Keyword.SetAvailable)) {
                return setAvailable(args, items);
            } else {
                throw new IllegalStatementException("Keyword not found", args);
            }
        } catch (

        ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

    private static ParameterizedStatement apply(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        String[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.To : Keyword.Apply);
        if (args.containsKey(Keyword.To) && peers.length == 0) {
            throw new IllegalArgumentException("Missing peers to apply the item to");
        } else if (args.containsKey(Keyword.Apply) && args.items(Keyword.Apply).length > 0) {
            throw new IllegalArgumentException(
                    "Apply just applies the default peers - use 'To' to apply additional peers");
        }
        DurationFormat duration = args.durationOption();
        boolean remember = args.rememberOption();
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) {
                Player player = state.player;
                for (String item : items) {
                    Item itemImpl = player.item(item);
                    Attributes attributeApplier = (StateMaps.Attributes) itemImpl;
                    attributeApplier.applyAttributes(player.script.scriptApplyAttribute);
                    attributeApplier.applyAttributes(player.namespaceApplyAttribute);
                    State.Options options = peers.length == 0 ? itemImpl.apply() : itemImpl.applyTo(peers);
                    args.handleStateOptions(options, duration, remember);
                }
            }

        };
    }

    private static ParameterizedStatement remove(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        if (args.containsKey(Keyword.To)) {
            throw new IllegalArgumentException(Keyword.Remove + " doesn't accept from/to peer list.");
        }
        String[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.From : Keyword.Remove);
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) {
                for (String item : items) {
                    if (peers.length == 0) {
                        state.player.item(item).remove();
                    } else {
                        state.player.item(item).removeFrom(peers);
                    }
                }
            }
        };
    }

    private static ParameterizedStatement setAvailable(final StateCommandLineParameters args, final String[] items) {
        final boolean setAvailable = Boolean.parseBoolean(args.value(Keyword.SetAvailable));
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) {
                for (String item : items) {
                    state.player.item(item).setAvailable(setAvailable);
                }
            }
        };
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
