package pcm.state;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.State.Persistence.Until;
import teaselib.util.DurationFormat;

public class BasicCommand implements Command {
    protected final ParameterizedCommandStatement statement;

    public BasicCommand(ParameterizedCommandStatement statement) {
        this.statement = statement;
    }

    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        statement.run(state);
    }

    protected static void handleStateOptions(State.Options options, DurationFormat duration, boolean remember) {
        if (duration != null) {
            var persistence = options.over(duration.toSeconds(), TimeUnit.SECONDS);
            if (remember) {
                persistence.remember(Until.Removed);
            }
        } else if (remember) {
            options.remember(Until.Removed);
        }
    }

    protected static ParameterizedCommandStatement remove(StateCommandLineParameters args, Statement statement,
            String[] items, BiFunction<Player, String, State> stateSupplier) {
        Object[] peers = args.optionalPeers(StateKeywords.Remove, StateKeywords.From);
        return new ParameterizedCommandStatement(statement, args) {
            @Override
            public void run(ScriptState scriptState) {
                for (String value : items) {
                    var state = stateSupplier.apply(scriptState.player, value);
                    if (peers.length == 0) {
                        state.remove();
                    } else {
                        state.removeFrom(peers);
                    }
                }
            }
        };
    }

}
