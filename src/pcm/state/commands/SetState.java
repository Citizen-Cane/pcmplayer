package pcm.state.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction.Statement;
import pcm.model.DurationFormat;
import pcm.model.IllegalStatementException;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.core.util.CommandLineParameters;
import teaselib.core.util.ReflectionUtils;

public class SetState extends BasicCommand {

    public enum Command {
        Apply,
        To,
        Over,
        Remember,
        Remove,
    }

    public SetState(String[] args) throws ScriptParsingException {
        super(statement(Statement.State, args));
    }

    private static ParameterizedStatement statement(final Statement statement, final String[] args)
            throws ScriptParsingException {
        final CommandLineParameters<Command> cmd = new CommandLineParameters<SetState.Command>(args, Command.values());
        try {
            if (cmd.containsKey(Command.Apply)) {
                final List<Enum<?>> items = ReflectionUtils.getEnums(cmd.get(Command.Apply));
                final Object[] attributes = ReflectionUtils.getEnums(cmd.get(Command.To)).toArray();
                final DurationFormat duration = cmd.containsKey(Command.Over)
                        ? new DurationFormat(cmd.get(Command.Over).get(0)) : null;
                final boolean remember = cmd.containsKey(Command.Remember);
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
            } else if (cmd.containsKey(Command.Remove)) {
                final List<Enum<?>> items = ReflectionUtils.getEnums(cmd.get(Command.Remove));
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
