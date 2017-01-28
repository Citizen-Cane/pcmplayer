package pcm.state.commands;

import pcm.state.Command;
import pcm.state.persistence.ScriptState;

public class Repeat implements Command {

	private final int n;
	private final int m;

	public Repeat(int n, int m) {
		this.n = n;
		this.m = m;
	}

	@Override
	public void execute(ScriptState state) {
		state.repeatSet(n, m);
	}

	@Override
	public String toString() {
		return Integer.toString(n) + ": " + Integer.toString(m);
	}
}
