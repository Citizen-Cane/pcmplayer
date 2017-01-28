package pcm.state.conditions;

import pcm.model.ActionRange;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

/**
 * The PCM version of "Greater or equal than": triggers if at least a number of
 * actions are set in a given range.
 *
 */
public class NumActionsAvailable extends ActionRange implements Condition {

    private final int x;

    public NumActionsAvailable(int n, int m, int x) {
        super(n, m);
        this.x = x;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        int numActionsAvailable = state.player.range(this).size();
        return numActionsAvailable >= x;
    }

    @Override
    public String toString() {
        return super.toString() + " " + Integer.toString(x);
    }
}
