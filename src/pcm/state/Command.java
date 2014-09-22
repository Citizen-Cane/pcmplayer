package pcm.state;

import pcm.model.ScriptExecutionError;


public interface Command {
	void execute(State state) throws ScriptExecutionError;
}
