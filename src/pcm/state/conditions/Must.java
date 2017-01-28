package pcm.state.conditions;

import java.util.HashSet;

import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class Must extends HashSet<Integer> implements Condition {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isTrueFor(ScriptState state) {
		for(Integer must : this)
		{
			if (!state.get(must).equals(ScriptState.SET))
			{
				return false;
			}
		}
		return true;
	}
}
