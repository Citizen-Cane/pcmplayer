package pcm.state.commands;

import pcm.model.ScriptExecutionException;
import pcm.state.Command;
import pcm.state.State;

public class Restore implements Command {
    @Override
    public void execute(State state) throws ScriptExecutionException {
        state.restore();
    }
}
