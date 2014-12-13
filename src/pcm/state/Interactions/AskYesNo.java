package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class AskYesNo implements Interaction {
    private final int startYes;
    private final int endYes;
    private final int startNo;
    private final int endNo;

    public AskYesNo(int startYes, int endYes, int startNo, int endNo) {
        this.startYes = startYes;
        this.endYes = endYes;
        this.startNo = startNo;
        this.endNo = endNo;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            final TeaseScript teaseScript) throws ScriptExecutionError {
        String yes = action.getResponseText(Statement.YesText, script);
        String no = action.getResponseText(Statement.NoText, script);
        TeaseLib.log("AskYesNo: '" + yes + "', '" + no + '+');
        final List<String> choices = new ArrayList<>();
        choices.add(yes);
        choices.add(no);
        visuals.run();
        if (teaseScript.choose(choices) == yes) {
            TeaseLib.log("-> Yes");
            return new ActionRange(startYes, endYes);
        } else {
            TeaseLib.log("-> No");
            return new ActionRange(startNo, endNo);
        }
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        script.actions.validate(action, new ActionRange(startYes, endYes),
                validationErrors);
        script.actions.validate(action, new ActionRange(startNo, endNo),
                validationErrors);
    }
}
