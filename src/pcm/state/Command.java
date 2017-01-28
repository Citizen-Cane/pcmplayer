package pcm.state;

import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;


public interface Command {
	void execute(ScriptState state) throws ScriptExecutionException;
}
