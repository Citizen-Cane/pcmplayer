package pcm.state;

import pcm.model.ScriptExecutionException;


public interface Command {
	void execute(State state) throws ScriptExecutionException;
}
