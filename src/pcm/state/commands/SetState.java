package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;
import teaselib.core.util.ReflectionUtils;

public class SetState extends BasicCommand {

    public enum Command {
        Apply,
        Remember,
        Remove,
    }

    public SetState(String[] args) throws ScriptParsingException {
        super(Statement.SetState, getCommandImplementation(args), args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static CommandImpl getCommandImplementation(String[] args)
            throws ScriptParsingException {
        try {
            Command command = Command.valueOf(args[0]);
            final Enum where = ReflectionUtils.getEnum(args[1]);
            if (command == Command.Apply) {
                final Enum<?> what = ReflectionUtils.getEnum(args[2]);
                if (args.length >= 4) {
                    String s = args[3];
                    final long minutes;
                    if (s.equalsIgnoreCase("INF")) {
                        minutes = State.INDEFINITELY;
                    } else if (s.equals(Command.Remember.name())) {
                        return new CommandImpl() {
                            @Override
                            public void execute(ScriptState state) {
                                state.player.state(where).apply(what)
                                        .remember();
                            }
                        };
                    } else {
                        minutes = Integer.parseInt(s);
                    }
                    if (args.length >= 5 && args[4]
                            .equalsIgnoreCase(Command.Remember.name())) {
                        return new CommandImpl() {
                            @Override
                            public void execute(ScriptState state) {
                                state.player.state(where).apply(what)
                                        .over(minutes, TimeUnit.MINUTES)
                                        .remember();
                            }
                        };
                    } else {
                        return new CommandImpl() {
                            @Override
                            public void execute(ScriptState state) {
                                state.player.state(where).apply(what)
                                        .over(minutes, TimeUnit.MINUTES);
                            }
                        };
                    }
                } else {
                    return new CommandImpl() {
                        @Override
                        public void execute(ScriptState state) {
                            state.player.state(where).apply(what);
                        }
                    };
                }
            } else if (command == Command.Remember) {
                throw new IllegalArgumentException(
                        command + " can only be used as an option for "
                                + Command.Apply.name());
            } else if (command == Command.Remove) {
                return new CommandImpl() {
                    @Override
                    public void execute(ScriptState state) {
                        state.player.state(where).remove();
                    }
                };
            } else {
                throw new IllegalComamndException(command);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptParsingException(e);
        }
    }

}
