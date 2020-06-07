package pcm.state.commands;

import pcm.model.ScriptExecutionException;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class DebugEntry implements Command {
    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        // Ignore
    }
}
