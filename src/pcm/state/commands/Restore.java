package pcm.state.commands;

import pcm.model.ScriptExecutionException;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class Restore implements Command {
    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        state.restore();
    }
}
