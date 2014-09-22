package pcm.state.commands;

import pcm.state.Command;
import pcm.state.State;

public class Repeat implements Command {

	private final int n;
	private final int m;

	public Repeat(int n, int m) {
		this.n = n;
		this.m = m;
	}

	@Override
	public void execute(State state) {
		state.repeatSet(n, m);
	}

	@Override
	public String toString() {
		return Integer.toString(n) + ": " + Integer.toString(m);
	}
}
