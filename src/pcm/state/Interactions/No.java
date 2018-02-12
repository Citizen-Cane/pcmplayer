package pcm.state.Interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import teaselib.Answer;

public class No extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(No.class);

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        String no = action.getResponseText(Statement.NoText, script);
        logger.info("Pause: " + no);
        player.reply(getConfidence(action), Answer.no(no));
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            action.getResponseText(Statement.NoText, script);
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }

        super.validate(script, action, validationErrors);
    }
}
