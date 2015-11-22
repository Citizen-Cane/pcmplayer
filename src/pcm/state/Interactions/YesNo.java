package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;

public class YesNo implements Interaction {
    private final int startYes;
    private final int endYes;
    private final int startNo;
    private final int endNo;

    public YesNo(int startYes, int endYes, int startNo, int endNo) {
        this.startYes = startYes;
        this.endYes = endYes;
        this.startNo = startNo;
        this.endNo = endNo;
    }

    @Override
    public ActionRange getRange(Script script, Action action,
            ScriptFunction visuals, final Player player)
            throws ScriptExecutionError {
        String yes = action.getResponseText(Statement.YesText, script);
        String no = action.getResponseText(Statement.NoText, script);
        TeaseLib.instance().log.info("AskYesNo: '" + yes + "', '" + no + '+');
        final List<String> choices = new ArrayList<String>();
        choices.add(yes);
        choices.add(no);
        visuals.run();
        if (player.reply(choices) == yes) {
            TeaseLib.instance().log.info("-> Yes");
            return new ActionRange(startYes, endYes);
        } else {
            TeaseLib.instance().log.info("-> No");
            return new ActionRange(startNo, endNo);
        }
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        script.actions.validate(script, action, new ActionRange(startYes,
                endYes), validationErrors);
        script.actions.validate(script, action,
                new ActionRange(startNo, endNo), validationErrors);
    }
}
