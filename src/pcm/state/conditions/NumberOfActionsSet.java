package pcm.state.conditions;

import pcm.model.ActionRange;
import pcm.state.Condition;
import pcm.state.State;

/**
 * The PCM version of "Greater or equal than": triggers if at least a number of
 * actions are set in a given range.
 *
 */
public class NumberOfActionsSet extends ActionRange implements Condition {

    private final int x;

    public NumberOfActionsSet(int n, int m, int x) {
        super(n, m);
        this.x = x;
    }

    @Override
    public boolean isTrueFor(State state) {
        int numberOfActionsSet = 0;
        for (Integer number : this) {
            if (state.get(number).equals(State.SET)) {
                numberOfActionsSet++;
            }
        }
        return numberOfActionsSet >= x;
    }

    @Override
    public String toString() {
        return super.toString() + " " + Integer.toString(x);
    }
}
