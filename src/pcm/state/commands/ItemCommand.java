package pcm.state.commands;

import pcm.model.AbstractAction;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptExecutionException;
import pcm.state.BasicCommand;
import pcm.state.ParameterizedCommandStatement;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.util.Item;

public class ItemCommand extends BasicCommand {
    private static final Statement ITEM = AbstractAction.Statement.Item;
    private final StateCommandLineParameters args;

    public ItemCommand(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    static ParameterizedCommandStatement statement(StateCommandLineParameters args) {
        String[] items = args.items(Keyword.Item);
        var declarations = args.getDeclarations();
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

    private static ParameterizedCommandStatement apply(StateCommandLineParameters args, String[] items) {
        return apply(args, ITEM, items, (player, item) -> player.item(item));
    }

    private static ParameterizedCommandStatement remove(StateCommandLineParameters args, String[] items) {
        return remove(args, ITEM, items, (player, item) -> player.item(item));
    }

    private static ParameterizedCommandStatement setAvailable(StateCommandLineParameters args, String[] items) {
        var setAvailable = Boolean.parseBoolean(args.value(Keyword.SetAvailable));
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
        String[] attributes = args.items(StateCommandLineParameters.Keyword.Matching);
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
