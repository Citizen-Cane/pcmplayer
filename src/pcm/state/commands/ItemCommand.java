package pcm.state.commands;

import static java.util.stream.Collectors.toList;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptExecutionException;
import pcm.state.BasicCommand;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.core.StateMaps;
import teaselib.core.StateMaps.Attributes;
import teaselib.core.util.QualifiedItem;
import teaselib.util.DurationFormat;
import teaselib.util.Item;
import teaselib.util.Items;

public class ItemCommand extends BasicCommand {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCommand(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args) {
        String[] items = args.items(Keyword.Item);
        Declarations declarations = args.getDeclarations();
        declarations.validate(items, Item.class);

        if (args.containsKey(StateCommandLineParameters.Keyword.Matching)) {
            return matching(args, items);
        } else if (args.containsKey(Keyword.Apply)) {
            return apply(args, items);
        } else if (args.containsKey(Keyword.Remove)) {
            return remove(args, items);
        } else if (args.containsKey(Keyword.SetAvailable)) {
            return setAvailable(args, items);
        } else {
            throw new IllegalStatementException("Keyword not found", args);
        }
    }

    private static ParameterizedStatement apply(final StateCommandLineParameters args, final String[] items) {
        Object[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.To : Keyword.Apply);
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

    private static ParameterizedStatement remove(final StateCommandLineParameters args, final String[] items) {
        if (args.containsKey(Keyword.To)) {
            throw new IllegalArgumentException(Keyword.Remove + " doesn't accept from/to peer list.");
        }
        Object[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.From : Keyword.Remove);
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

    private static ParameterizedStatement matching(final StateCommandLineParameters args, final String[] items) {
        String[] attributes = args.items(StateCommandLineParameters.Keyword.Matching);
        return new ParameterizedStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) throws ScriptExecutionException {
                replaceWithMatching(args, items, attributes, state);
                statement(args).run(state);
            }
        };
    }

    public static void replaceWithMatching(StateCommandLineParameters args, String[] items, String[] attributes,
            ScriptState state) {
        Items matching = state.player.items(items).matching(attributes);
        args.remove(Keyword.Matching);
        args.remove(Keyword.Item);
        args.put(Keyword.Item, matching.stream().map(QualifiedItem::of).map(QualifiedItem::toString).collect(toList()));
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
