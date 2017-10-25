package pcm.state.Interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;

/**
 * @author someone
 *
 *         Calls a sub program specified by the range argument after rendering the visuals of the action.
 * 
 */
public class GoSub extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(GoSub.class);

    final private ActionRange range;

    public GoSub(ActionRange range) {
        this.range = range;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        logger.info(range.toString());
        visuals.run();
        script.stack.push(rangeProvider.getRange(player, script, action, NoVisuals));
        return range;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        range.validate();

        super.validate(script, action, validationErrors);
    }
}
