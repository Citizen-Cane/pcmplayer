/**
 * 
 */
package pcm.state.conditions;

import pcm.state.Command;
import pcm.state.persistence.ScriptState;

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
    public boolean isTrueFor(ScriptState state) {
        return state.get(n) == ScriptState.UNSET;
    }

    @Override
    public String toString() {
        return Integer.toString(n) + " then "
                + command.getClass().getSimpleName() + " " + command.toString();
    }
}
