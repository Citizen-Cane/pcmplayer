package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import pcm.controller.StateCommandLineParameters;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class StateCommand extends BasicCommand {

    private static final Statement STATE = Statement.State;

    public StateCommand(StateCommandLineParameters args) throws ScriptParsingException {
        super(statement(args));
    }

    private static ParameterizedStatement statement(final StateCommandLineParameters args)
            throws ScriptParsingException {
        try {
            final Enum<?>[] items = args.items();
            if (args.containsKey(StateCommandLineParameters.Keyword.Apply)) {
                final Object[] attributes = args.options(StateCommandLineParameters.Keyword.To);
                final DurationFormat duration = args.durationOption();
                final boolean remember = args.rememberOption();
                return new ParameterizedStatement(STATE, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            State.Options options = state.player.state(item).apply(attributes);
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
            } else if (args.containsKey(StateCommandLineParameters.Keyword.Remove)) {
                return new ParameterizedStatement(STATE, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            state.player.state(item).remove();
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

}
