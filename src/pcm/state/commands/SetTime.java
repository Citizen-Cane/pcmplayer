package pcm.state.commands;

import java.util.Date;

import pcm.state.Command;
import pcm.state.State;

public class SetTime implements Command {

	final int n;

	public SetTime(int n)
	{
		this.n = n;
	}

	@Override
	public void execute(State state) {
		long time = state.getTime() * 1000;
		state.setTime(n, new Date(time));
	}

	@Override
	public String toString() {
		return Integer.toString(n); 
	}
}
