package pcm.state.commands;

import java.util.function.BiFunction;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCommand;
import pcm.state.ParameterizedCommandStatement;
import pcm.state.StateCommandLineParameters;
import pcm.state.StateKeywords;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.State.Options;
import teaselib.util.DurationFormat;

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
        return apply(args, STATE, items, (player, state) -> player.state(state), (item, peers) -> item.applyTo(peers));
    }

    private static ParameterizedCommandStatement apply(//
            StateCommandLineParameters args, Statement statement, String[] items, //
            BiFunction<Player, String, State> supplier, BiFunction<State, Object[], Options> applier) {
        Object[] peers = args.optionalPeers(StateKeywords.Apply, StateKeywords.To);
        DurationFormat duration = args.durationOption();
        boolean remember = args.rememberOption();

        return new ParameterizedCommandStatement(statement, args) {
            @Override
            public void run(ScriptState scriptState) {
                var player = scriptState.player;
                Object[] attributes = { player.script.scriptApplyAttribute, player.namespaceApplyAttribute };
                for (String name : items) {
                    State item = supplier.apply(player, name);
                    ((State.Attributes) item).applyAttributes(attributes);
                    var options = peers.length == 0 ? item.apply() : applier.apply(item, peers);
                    handleStateOptions(options, duration, remember);
                }
            }
        };
    }

    private static ParameterizedCommandStatement remove(StateCommandLineParameters args, final String[] items) {
        return remove(args, STATE, items, (player, item) -> player.state(item));
    }

    @Override
    public String toString() {
        return args.toString();
    }

}
