package pcm.state.commands;

import pcm.state.Command;
import pcm.state.State;

public class RepeatAdd implements Command {

	private final int n;
	private final int y;
	private final int z;

	public RepeatAdd(int n) {
		this.n = n;
		this.y = 1;
		this.z = 1;
	}
	public RepeatAdd(int n, int y, int z) {
		this.n = n;
		this.y = y;
		this.z = z;
	}

	@Override
	public void execute(State state) {
		state.repeatAdd(n, state.getRandom(y, z));
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
