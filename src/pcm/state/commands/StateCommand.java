package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import pcm.controller.Player;
import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
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
            final String[] items = args.items(Keyword.Item);
            if (args.containsKey(Keyword.Apply)) {
                final String[] peers = args.items(args.containsKey(Keyword.To) ? Keyword.To : Keyword.Apply);
                if (args.containsKey(Keyword.To) && peers.length == 0) {
                    throw new IllegalArgumentException("Missing peers to apply the item to");
                }
                final DurationFormat duration = args.durationOption();
                final boolean remember = args.rememberOption();
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
                                State.Persistence persistence = options.over(duration.toSeconds(), TimeUnit.SECONDS);
                                if (remember) {
                                    persistence.remember();
                                }
                            } else if (remember) {
                                options.remember();
                            }
                        }
                    }
                };
            } else if (args.containsKey(Keyword.Remove)) {
                final String[] peers = args.items(Keyword.From);
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
            } else {
                throw new IllegalStatementException(STATE, args);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }

    }

    @Override
    public String toString() {
        return args.toString();
    }

}
