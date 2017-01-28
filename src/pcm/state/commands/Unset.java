package pcm.state.commands;

import java.util.HashSet;

import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class Unset extends HashSet<Integer> implements Command {
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(ScriptState state) {
		state.unset(this);
	}
}
