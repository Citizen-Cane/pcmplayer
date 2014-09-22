package pcm.state.conditions;

import pcm.state.Condition;
import pcm.state.State;

/**
 * @author someone
 * The action will not run until at least n actions have run since command number m was given. 
 * The count starts from when action m is first given - not from the last occurrence. 
 */
public class NumActionsFrom implements Condition {

	final int action;
	final int numActionsFrom;
	
	public NumActionsFrom(int action, int numActionsFrom) {
		super();
		this.action = action;
		this.numActionsFrom = numActionsFrom;
	}

	@Override
	public boolean isTrueFor(State state) {
		int n = state.getStep(action) + numActionsFrom; 
		return state.getStep() > n;
	}

	@Override
	public String toString() {
		return action + " " + numActionsFrom; 
	}
}
