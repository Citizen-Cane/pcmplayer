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
import pcm.state.Interaction;

public class Pause implements Interaction, Interaction.NeedsRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Pause.class);

    private Interaction rangeProvider = null;

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) throws ScriptExecutionException {
        String resume = action.getResponseText(Statement.ResumeText, script);
        logger.info("Pause: " + resume);
        List<String> choices = new ArrayList<String>(1);
        choices.add(resume);
        visuals.run();
        player.reply(choices);
        return rangeProvider.getRange(script, action, NoVisuals, player);
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        if (this.rangeProvider != null) {
            if (this.rangeProvider instanceof NeedsRangeProvider) {
                ((NeedsRangeProvider) this.rangeProvider)
                        .setRangeProvider(rangeProvider);
            } else {
                throw new IllegalStateException(rangeProvider.toString());
            }
        } else {
            this.rangeProvider = rangeProvider;
        }
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            action.getResponseText(Statement.ResumeText, script);
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
