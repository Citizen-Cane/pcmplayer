package pcm.state.commands;

import java.util.HashSet;

import pcm.state.Command;
import pcm.state.State;

public class Unset extends HashSet<Integer> implements Command {
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(State state) {
		state.unset(this);
	}
}
