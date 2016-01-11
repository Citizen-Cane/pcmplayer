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
public class IfSet extends IfClause {
    final int n;

    public IfSet(int n, Command command) {
        super(command);
        this.n = n;
    }

    @Override
    public boolean isTrueFor(State state) {
        return state.get(n) != State.UNSET;
    }

    @Override
    public String toString() {
        return Integer.toString(n) + " then "
                + command.getClass().getSimpleName() + " " + command.toString();
    }
}
