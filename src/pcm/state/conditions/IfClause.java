/**
 * 
 */
package pcm.state.conditions;

import pcm.model.ScriptExecutionError;
import pcm.state.Command;
import pcm.state.State;
import teaselib.TeaseLib;

/**
 * @author someone
 *
 */
public abstract class IfClause implements Command {
    final Command command;

    public IfClause(Command command) {
        super();
        this.command = command;
    }

    @Override
    public void execute(State state) throws ScriptExecutionError {
        if (isTrueFor(state)) {
            TeaseLib.instance().log.info(" -> "
                    + command.getClass().getSimpleName() + " "
                    + command.toString());
            command.execute(state);
        }
    }

    public abstract boolean isTrueFor(State state);

    @Override
    public abstract String toString();
}
