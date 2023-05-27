package pcm.state.interactions;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class Range implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(Range.class);

    ActionRange actionRange;

    public Range(ActionRange range) {
        this.actionRange = range;
    }

    @Override
    public Action getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        logger.info("{}", actionRange);
        return player.getAction(actionRange);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        script.actions.validate(script, action, actionRange, validationErrors);
    }

    @Override
    public List<ActionRange> coverage() {
        return Collections.singletonList(actionRange);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(getClass().getSimpleName());
        string.append(" ");
        string.append(actionRange);
        return string.toString();
    }

}
