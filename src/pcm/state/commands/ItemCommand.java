package pcm.state.commands;

import static java.util.stream.Collectors.toList;
import static pcm.state.StateCommandLineParameters.Keyword.From;
import static pcm.state.StateCommandLineParameters.Keyword.Remove;

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

    static ParameterizedStatement statement(final StateCommandLineParameters args) {
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

    private static ParameterizedStatement apply(StateCommandLineParameters args, String[] items) {
        Keyword to = Keyword.To;
        Keyword apply = Keyword.Apply;
        Object[] peers = optionalPeers(args, apply, to);
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

    private static ParameterizedStatement remove(StateCommandLineParameters args, String[] items) {
        Object[] peers = BasicCommand.optionalPeers(args, Remove, From);
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
