package pcm.state.commands;

import pcm.state.Command;
import pcm.state.State;

public class RepeatDel implements Command {

	private final int n;
	private final int y;
	private final int z;

	public RepeatDel(int n) {
		this.n = n;
		this.y = 1;
		this.z = 1;
	}

	public RepeatDel(int n, int y, int z) {
		this.n = n;
		this.y = y;
		this.z = z;
	}

	@Override
	public void execute(State state) {
		state.repeatDel(n, state.getRandom(y, z));
	}

	@Override
	public String toString() {
		if (y > 1 && z > y)
		{
			return Integer.toString(n) + " " + Integer.toString(y) + " " + Integer.toString(z);
		}
		else
		{
			return Integer.toString(n);
		}
	}
}
