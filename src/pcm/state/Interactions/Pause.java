package pcm.state.Interactions;

import java.util.ArrayList;
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

public class Pause extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Pause.class);

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        String resume = action.getResponseText(Statement.ResumeText, script);
        List<String> choices = new ArrayList<>(1);
        choices.add(resume);
        visuals.run();
        logger.info("Pause: " + resume);
        player.reply(getConfidence(action), choices);
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            action.getResponseText(Statement.ResumeText, script);
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }

        super.validate(script, action, validationErrors);
    }
}
