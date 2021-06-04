package pcm.state;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.state.StateCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;
import teaselib.util.DurationFormat;

public abstract class ParameterizedConditionStatement extends ParameterizedStatement {

    protected <T extends Enum<T>> ParameterizedConditionStatement(Statement statement, CommandLineParameters<T> args) {
        super(statement, args);
    }

    public abstract boolean call(ScriptState state);

    @Override
    public String toString() {
        return statement.toString() + " " + args.toString();
    }

    public static class Boolean extends ParameterizedConditionStatement {

        private final String[] items;
        private final BiPredicate<Player, String> flagSupplier;

        public Boolean(StateCommandLineParameters args, String[] items, Statement statement,
                BiPredicate<Player, String> flagSupplier) {
            super(statement, args);
            this.items = items;
            this.flagSupplier = flagSupplier;
        }

        @Override
        public boolean call(ScriptState state) {
            for (String item : items) {
                if (!flagSupplier.test(state.player, item)) {
                    return false;
                }
            }
            return true;
        }

    }

    public static class Duration extends ParameterizedConditionStatement {

        private final String[] items;
        private final DurationFormat durationFormat;
        private final StateCommandLineParameters.Operator comperator;
        private final BiFunction<Player, String, Long> durationSupplier;

        public Duration(StateCommandLineParameters args, String[] items, Statement statement,
                BiFunction<Player, String, Long> duration) {
            super(statement, args);
            this.items = items;
            this.durationSupplier = duration;
            Keyword condition = args.getCondition();
            this.durationFormat = new DurationFormat(args.value(condition));
            this.comperator = args.getOperator(condition);
        }

        @Override
        public boolean call(ScriptState state) {
            for (String item : items) {
                if (!(comperator.isTrueFor(durationSupplier.apply(state.player, item), durationFormat.toSeconds()))) {
                    return false;
                }
            }
            return true;
        }

    }

}