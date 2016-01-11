/**
 * 
 */
package pcm.state.conditions;

import pcm.state.Command;
import pcm.state.State;

/**
 * @author someone
 *
 */
public class IfUnset extends IfClause {
    final int n;

    public IfUnset(int n, Command command) {
        super(command);
        this.n = n;
    }

    @Override
    public boolean isTrueFor(State state) {
        return state.get(n) == State.UNSET;
    }

    @Override
    public String toString() {
        return Integer.toString(n) + " then "
                + command.getClass().getSimpleName() + " " + command.toString();
    }
}
