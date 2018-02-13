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

public class AbstractPause extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractPause.class);

    private final Statement textType;
    private final Answer.Meaning meaning;

    public AbstractPause(Statement textType, Answer.Meaning meaning) {
        this.textType = textType;
        this.meaning = meaning;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();

        String text = action.getResponseText(textType, script);
        logger.info(getClass().getSimpleName() + ": " + text);
        player.reply(getConfidence(action), new Answer(text, meaning));
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            action.getResponseText(textType, script);
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }

        super.validate(script, action, validationErrors);
    }
}
