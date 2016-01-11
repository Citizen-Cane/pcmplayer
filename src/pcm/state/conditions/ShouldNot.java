package pcm.state.conditions;

import java.util.HashSet;

import pcm.state.Condition;
import pcm.state.State;

public class ShouldNot extends HashSet<Integer> implements Condition {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isTrueFor(State state) {
		for(Integer mustnot : this)
		{
			if (state.get(mustnot).equals(State.SET))
			{
				return false;
			}
		}
		return true;
	}


}
