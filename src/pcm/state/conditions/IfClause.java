/**
 * 
 */
package pcm.state.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.ScriptExecutionException;
import pcm.state.Command;
import pcm.state.persistence.ScriptState;

/**
 * @author someone
 *
 */
public abstract class IfClause implements Command {
    private static final Logger logger = LoggerFactory
            .getLogger(IfClause.class);

    final Command command;

    public IfClause(Command command) {
        super();
        this.command = command;
    }

    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        if (isTrueFor(state)) {
            logger.info(" -> " + command.getClass().getSimpleName() + " "
                    + command.toString());
            command.execute(state);
        }
    }

    public abstract boolean isTrueFor(ScriptState state);

    @Override
    public abstract String toString();
}
