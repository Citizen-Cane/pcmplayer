package pcm.state.commands;

import java.util.concurrent.TimeUnit;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCommand;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class SetState extends BasicCommand {

    public enum Command {
        Apply,
        Remember,
        Clear,
    }

    public SetState(String[] args) throws ScriptParsingException {
        super(Statement.SetState, getCommandImplementation(args), args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static CommandImpl getCommandImplementation(String[] args)
            throws ScriptParsingException {
        try {
            Command command = Command.valueOf(args[0]);
            final Enum where = getEnum(args[1]);
            if (command == Command.Apply) {
                final Enum<?> what = getEnum(args[2]);
                if (args.length >= 4) {
                    String s = args[3];
                    final long minutes;
                    if (s.equals("ÎNF")) {
                        minutes = State.INFINITE;
                    } else {
                        minutes = Integer.parseInt(s);
                    }
                    return new CommandImpl() {
                        @Override
                        public void execute(ScriptState state) {
                            state.player.state(where).apply(what, minutes,
                                    TimeUnit.MINUTES);
                        }
                    };
                } else {
                    return new CommandImpl() {
                        @Override
                        public void execute(ScriptState state) {
                            state.player.state(where).apply(what);
                        }
                    };
                }
            } else if (command == Command.Remember) {
                return new CommandImpl() {
                    @Override
                    public void execute(ScriptState state) {
                        state.player.state(where).remember();
                    }
                };
            } else if (command == Command.Clear) {
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
