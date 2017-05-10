package pcm.state.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import pcm.controller.StateCommandLineParameters;
import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class SetState extends BasicCommand {

    public SetState(String[] args) throws ScriptParsingException {
        super(statement(Statement.State, args));
    }

    private static ParameterizedStatement statement(final Statement statement, final String[] args)
            throws ScriptParsingException {
        final StateCommandLineParameters cmd = new StateCommandLineParameters(args);
        try {
            if (cmd.containsKey(StateCommandLineParameters.Keyword.Apply)) {
                final List<Enum<?>> items = cmd.applyOptions();
                final Object[] attributes = cmd.toOptions();
                final DurationFormat duration = cmd.durationOption();
                final boolean remember = cmd.rememberOption();
                return new ParameterizedStatement(statement, args) {
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
            } else if (cmd.containsKey(StateCommandLineParameters.Keyword.Remove)) {
                final List<Enum<?>> items = cmd.removeOptions();
                return new ParameterizedStatement(statement, args) {
                    @Override
                    public void run(ScriptState state) {
                        for (Enum<?> item : items) {
                            state.player.state(item).remove();
                        }
                    }
                };
            } else {
                throw new IllegalStatementException(statement, args);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }

    }

}
