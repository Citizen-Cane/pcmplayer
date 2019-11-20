package pcm.state.interactions;

import java.util.Arrays;
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
import pcm.state.Interaction;
import teaselib.Answer;

public class YesNo implements Interaction {
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
    public Action getRange(final Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        Answer yes = Answer.yes(action.getResponseText(Statement.YesText, script));
        Answer no = Answer.no(action.getResponseText(Statement.NoText, script));
        ActionRange range;
        if (player.reply(yes, no) == yes) {
            logger.info("-> Yes");
            range = ActionRange.of(startYes, endYes);
        } else {
            logger.info("-> No");
            range = ActionRange.of(startNo, endNo);
        }
        return player.getAction(range);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        try {
            action.getResponseText(Statement.YesText, script);
            action.getResponseText(Statement.NoText, script);
        } catch (Exception e) {
            validationErrors.add(new ValidationIssue(script, action, e));
        }
        script.actions.validate(script, action, new ActionRange(startYes, endYes), validationErrors);
        script.actions.validate(script, action, new ActionRange(startNo, endNo), validationErrors);
    }

    @Override
    public List<ActionRange> coverage() {
        return Arrays.asList(new ActionRange(startYes, endYes), new ActionRange(startNo, endNo));
    }

}
