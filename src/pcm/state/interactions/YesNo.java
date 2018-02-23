package pcm.state.interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import teaselib.Answer;

public class YesNo extends AbstractInteraction {
    private static final Logger logger = LoggerFactory.getLogger(YesNo.class);

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
    public ActionRange getRange(final Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        String yes = action.getResponseText(Statement.YesText, script);
        String no = action.getResponseText(Statement.NoText, script);
        if (player.reply(getConfidence(action), new Answer[] { Answer.yes(yes), Answer.no(no) }) == yes) {
            logger.info("-> Yes");
            return new ActionRange(startYes, endYes);
        } else {
            logger.info("-> No");
            return new ActionRange(startNo, endNo);
        }
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        try {
            action.getResponseText(Statement.YesText, script);
            action.getResponseText(Statement.NoText, script);
        } catch (Exception e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
        script.actions.validate(script, action, new ActionRange(startYes, endYes), validationErrors);
        script.actions.validate(script, action, new ActionRange(startNo, endNo), validationErrors);
    }
}
