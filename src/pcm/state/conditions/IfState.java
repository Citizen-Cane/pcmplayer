package pcm.state.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pcm.controller.Declarations;
import pcm.controller.StateCommandLineParameters;
import pcm.controller.StateCommandLineParameters.Keyword;
import pcm.model.AbstractAction.Statement;
import pcm.model.ScriptParsingException;
import pcm.state.BasicCondition;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class IfState extends BasicCondition {

    public interface ConditionCreator {
        Condition createCondition(StateCommandLineParameters firstParameters) throws ScriptParsingException;

    }

    public IfState(Statement statement, String[] args, ConditionCreator conditionCreator, Declarations declarations)
            throws ScriptParsingException {
        super(statement(statement, args, conditionCreator, declarations));
    }

    private static ParameterizedStatement statement(Statement statement, String[] args,
            ConditionCreator conditionCreator, Declarations declarations) throws ScriptParsingException {
        final List<Condition> conditions = new ArrayList<Condition>();

        int i = 0;
        List<String> firstCondition = new ArrayList<String>();
        while (i < args.length && !("or".equalsIgnoreCase(args[i]))) {
            firstCondition.add(args[i++]);
        }
        i++;

        StateCommandLineParameters firstParameters = new StateCommandLineParameters(firstCondition, declarations);
        conditions.add(conditionCreator.createCondition(firstParameters));

        while (i < args.length) {
            List<String> conditionArgs = new ArrayList<String>();
            conditionArgs.addAll(Arrays.asList(firstParameters.values(Keyword.Item)));
            while (i < args.length && !("or".equalsIgnoreCase(args[i]))) {
                conditionArgs.add(args[i++]);
            }
            i++;

            StateCommandLineParameters conditionParameters = new StateCommandLineParameters(conditionArgs,
                    declarations);
            conditions.add(conditionCreator.createCondition(conditionParameters));
        }

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

}
