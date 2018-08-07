package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import pcm.controller.Player;
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

public class StateCommand extends BasicCommand {

    private static final Statement STATE = Statement.State;
    private final StateCommandLineParameters args;

    public StateCommand(StateCommandLineParameters args) throws ScriptParsingException {
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
            } else {
                throw new IllegalStatementException("Keyword not found", args);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }

    }

    private static ParameterizedStatement apply(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        Object[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.To : Keyword.Apply);
        if (args.containsKey(Keyword.To) && peers.length == 0) {
            throw new IllegalArgumentException("Missing peers to apply the item to");
        }
        DurationFormat duration = args.durationOption();
        boolean remember = args.rememberOption();
        return new ParameterizedStatement(STATE, args) {
            @Override
            public void run(ScriptState state) {
                Player player = state.player;
                for (String item : items) {
                    Attributes attributeApplier = (StateMaps.Attributes) player.state(item);
                    attributeApplier.applyAttributes(player.script.scriptApplyAttribute);
                    attributeApplier.applyAttributes(player.namespaceApplyAttribute);
                    State.Options options = state.player.state(item).applyTo(peers);
                    if (duration != null) {
                        State persistence = options.over(duration.toSeconds(), TimeUnit.SECONDS);
                        if (remember) {
                            throw new UnsupportedOperationException();
                            // persistence.remember();
                        }
                    } else if (remember) {
                        // options.remember();
                    }
                }
            }
        };
    }

    private static ParameterizedStatement remove(final StateCommandLineParameters args, final String[] items)
            throws ClassNotFoundException {
        Object[] peers = args.items(Keyword.From);
        return new ParameterizedStatement(STATE, args) {
            @Override
            public void run(ScriptState state) {
                for (String item : items) {
                    if (peers.length == 0) {
                        state.player.state(item).remove();
                    } else {
                        state.player.state(item).removeFrom(peers);
                    }
                }
            }
        };
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
