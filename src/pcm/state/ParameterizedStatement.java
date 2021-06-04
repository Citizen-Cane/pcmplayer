package pcm.state;

import pcm.model.AbstractAction.Statement;
import teaselib.core.util.CommandLineParameters;

public abstract class ParameterizedStatement {
    protected final Statement statement;
    protected final CommandLineParameters<?> args;

    protected <T extends Enum<T>> ParameterizedStatement(Statement statement, CommandLineParameters<T> args) {
        this.statement = statement;
        this.args = args;
    }

    @Override
    public String toString() {
        return statement.toString() + " " + args.toString();
    }

}
