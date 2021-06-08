package pcm.state.commands;

import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCommand;
import pcm.state.StateKeywords;
import pcm.state.ParameterizedCommandStatement;
import pcm.state.StateCommandLineParameters;
import teaselib.State;

public class StateCommand extends BasicCommand {

    private static final Statement STATE = Statement.State;
    private final StateCommandLineParameters args;

    public StateCommand(StateCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    private static ParameterizedCommandStatement statement(StateCommandLineParameters args) {
        String[] items = args.items(StateKeywords.Item);
        args.getDeclarations().validate(items, State.class);

        if (args.containsKey(StateKeywords.Apply)) {
            return apply(args, items);
        } else if (args.containsKey(StateKeywords.Remove)) {
            return remove(args, items);
        } else {
            throw new IllegalStatementException("State command not found or invalid", args);
        }
    }

    private static ParameterizedCommandStatement apply(StateCommandLineParameters args, final String[] items) {
        return apply(args, STATE, items, (player, state) -> player.state(state));
    }

    private static ParameterizedCommandStatement remove(StateCommandLineParameters args, final String[] items) {
        return remove(args, STATE, items, (player, item) -> player.state(item));
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
