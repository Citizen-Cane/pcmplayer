package pcm.state.commands;

import pcm.model.ActionRange;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class Save extends ActionRange implements Command {

	public Save(int start, int end) {
		super(start, end);
	}

	@Override
	public void execute(ScriptState state) {
		state.save(this);
	}

	@Override
	public String toString() {
		return Integer.toString(start) + " " + Integer.toString(end); 
	}
}
