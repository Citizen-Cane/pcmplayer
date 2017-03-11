package pcm.state;

import java.util.Arrays;

import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptExecutionException;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.ReflectionUtils;

public class BasicStatement {
    private final Statement statement;
    private final String[] args;
    protected final CommandImpl command;

    public static class IllegalComamndException
            extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public IllegalComamndException(Object command) {
            super("Unknown command " + command.toString());
        }

    }

    protected interface CommandImpl {
        void execute(ScriptState state) throws ScriptExecutionException;
    }

    public BasicStatement(Statement statement, CommandImpl command,
            String[] args) {
        this.statement = statement;
        this.command = command;
        this.args = Arrays.copyOf(args, 1);
    }

    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    protected static Enum getEnum(String enumValue)
            throws ClassNotFoundException {
        Class<?> enumClass = Class
                .forName(ReflectionUtils.getEnumClass(enumValue));
        return (Enum) Enum.valueOf((Class<? extends Enum>) enumClass,
                ReflectionUtils.getEnmumValue(enumValue));
    }

    @Override
    public String toString() {
        return statement.toString() + args.toString();
    }

}
