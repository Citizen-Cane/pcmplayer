package pcm.state;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;

public abstract class ParameterizedCommandStatement extends ParameterizedStatement {

    protected <T extends Enum<T>> ParameterizedCommandStatement(Statement statement, CommandLineParameters<T> args) {
        super(statement, args);
    }

    public abstract void run(ScriptState state) throws ScriptExecutionException;

}
