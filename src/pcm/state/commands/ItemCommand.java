package pcm.state.commands;

import java.util.function.BiFunction;

import pcm.controller.Player;
import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptExecutionException;
import pcm.state.BasicCommand;
import pcm.state.ParameterizedCommandStatement;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateKeywords;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.State.Options;
import teaselib.util.DurationFormat;
import teaselib.util.Item;

public class ItemCommand extends BasicCommand {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCommand(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    static ParameterizedCommandStatement statement(StateCommandLineParameters args) {
        String[] items = args.items(StateKeywords.Item);
        var declarations = args.getDeclarations();
        declarations.validate(items, Item.class);

        if (args.containsKey(StateKeywords.Matching)) {
            return matching(args, items);
        } else if (args.containsKey(StateKeywords.Apply)) {
            return apply(args, items);
        } else if (args.containsKey(StateKeywords.Remove)) {
            return remove(args, items);
        } else if (args.containsKey(StateKeywords.SetAvailable)) {
            return setAvailable(args, items);
        } else {
            throw new IllegalStatementException("Item command not found or invalid", args);
        }
    }

    // TODO PCM can handle only one item of a kind, e.g. not multiple dildos for filling all holes

    private static ParameterizedCommandStatement apply(StateCommandLineParameters args, String[] items) {
        return apply(args, ITEM, items, (player, item) -> player.item(item), (item, peers) -> item.to(peers).apply());
    }

    protected static ParameterizedCommandStatement apply(//
            StateCommandLineParameters args, Statement statement, String[] items, //
            BiFunction<Player, String, Item> supplier, BiFunction<Item, Object[], Options> applier) {
        Object[] peers = args.optionalPeers(StateKeywords.Apply, StateKeywords.To);
        DurationFormat duration = args.durationOption();
        boolean remember = args.rememberOption();

        return new ParameterizedCommandStatement(statement, args) {
            @Override
            public void run(ScriptState scriptState) {
                var player = scriptState.player;
                Object[] attributes = { player.script.scriptApplyAttribute, player.namespaceApplyAttribute };
                for (String name : items) {
                    Item item = supplier.apply(player, name);
                    ((State.Attributes) item).applyAttributes(attributes);
                    var options = peers.length == 0 ? item.apply() : applier.apply(item, peers);
                    handleStateOptions(options, duration, remember);
                }
            }
        };
    }

    private static ParameterizedCommandStatement remove(StateCommandLineParameters args, String[] items) {
        return remove(args, ITEM, items, (player, item) -> player.item(item));
    }

    // TODO Review: items are applied as similar items but removed as custom peers -> default peers not removed
    // - This is going to be a problem for items with default peers, okay for items without

    private static ParameterizedCommandStatement setAvailable(StateCommandLineParameters args, String[] items) {
        var setAvailable = Boolean.parseBoolean(args.value(StateKeywords.SetAvailable));
        return new ParameterizedCommandStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) {
                for (String item : items) {
                    state.player.item(item).setAvailable(setAvailable);
                }
            }
        };
    }

    private static ParameterizedCommandStatement matching(StateCommandLineParameters args, String[] items) {
        String[] attributes = args.items(StateKeywords.Matching);
        return new ParameterizedCommandStatement(ITEM, args) {
            @Override
            public void run(ScriptState state) throws ScriptExecutionException {
                ((StateCommandLineParameters) args).replaceWithMatching(items, attributes, state);
                statement((StateCommandLineParameters) args).run(state);
            }
        };
    }

    @Override
    public String toString() {
        return args.toString();
    }

}
