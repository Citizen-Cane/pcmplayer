package pcm.state.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pcm.controller.Declarations;
import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class IfState extends BasicCondition {

    private static final String AND = "AND";
    private static final String OR = "OR";

    public interface ConditionCreator {
        Condition createCondition(StateCommandLineParameters firstParameters) throws ScriptParsingException;

    }

    private static final Set<String> OPERATORS = new HashSet<String>(Arrays.asList(OR, AND));

    public IfState(Statement statement, String[] args, ConditionCreator conditionCreator, Declarations declarations)
            throws ScriptParsingException {
        super(statement(statement, args, conditionCreator, declarations));
    }

    private static ParameterizedStatement statement(Statement statement, String[] args,
            ConditionCreator conditionCreator, Declarations declarations) throws ScriptParsingException {
        final List<Condition> conditions = new ArrayList<Condition>();

        int i = 0;
        List<String> firstCondition = new ArrayList<String>();
        while (i < args.length && !isOperator(args[i])) {
            firstCondition.add(args[i++]);
        }
        String alwaysTheSameOperator = i < args.length ? args[i] : "";
        i++;

        StateCommandLineParameters firstParameters = new StateCommandLineParameters(firstCondition, declarations);
        conditions.add(conditionCreator.createCondition(firstParameters));

        while (i < args.length) {
            List<String> conditionArgs = new ArrayList<String>();
            conditionArgs.addAll(Arrays.asList(firstParameters.values(Keyword.Item)));
            while (i < args.length && !isOperator(args[i])) {
                conditionArgs.add(args[i++]);
            }

            if (i < args.length && !args[i].equalsIgnoreCase(alwaysTheSameOperator)) {
                throw new IllegalArgumentException(
                        "Operators " + alwaysTheSameOperator + " " + args[i] + "can't be mixed");
            }

            i++;

            StateCommandLineParameters conditionParameters = new StateCommandLineParameters(conditionArgs,
                    declarations);
            conditions.add(conditionCreator.createCondition(conditionParameters));
        }
        if (alwaysTheSameOperator.equalsIgnoreCase(AND)) {
            return logicalAND(statement, args, declarations, conditions);
        } else if (alwaysTheSameOperator.equalsIgnoreCase(OR)) {
            return logicalOR(statement, args, declarations, conditions);
        } else {
            throw new IllegalStateException();
        }
    }

    private static ParameterizedStatement logicalAND(Statement statement, String[] args, Declarations declarations,
            final List<Condition> conditions) {
        return new ParameterizedStatement(statement, new StateCommandLineParameters(args, declarations)) {
            @Override
            protected boolean call(ScriptState state) {
                for (Condition condition : conditions) {
                    if (!condition.isTrueFor(state))
                        return false;
                }
                return true;
            }
        };
    }

    private static ParameterizedStatement logicalOR(Statement statement, String[] args, Declarations declarations,
            final List<Condition> conditions) {
        return new ParameterizedStatement(statement, new StateCommandLineParameters(args, declarations)) {
            @Override
            protected boolean call(ScriptState state) {
                for (Condition condition : conditions) {
                    if (condition.isTrueFor(state))
                        return true;
                }
                return false;
            }
        };
    }

    private static boolean isOperator(String value) {
        for (String operator : OPERATORS) {
            if (operator.equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    public static boolean isExtendedIfClause(String[] args) {
        for (String arg : args) {
            if (isOperator(arg)) {
                return true;
            }
        }
        return false;
    }
}
