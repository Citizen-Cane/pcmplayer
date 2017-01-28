package pcm.state.commands;

import pcm.model.ActionRange;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class ResetRange extends ActionRange implements Command {

	public ResetRange(int start) {
		super(start);
	}
	
	public ResetRange(int start, int end) {
		super(start, end);
	}

	@Override
	public void execute(ScriptState state) {
		state.resetRange(start, end);
	}

	@Override
	public String toString() {
		return Integer.toString(start) + " " + Integer.toString(end); 
	}
}
