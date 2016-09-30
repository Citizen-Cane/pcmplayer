package pcm.state.Interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class Range implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(Range.class);

    private final int start;
    private final int end;

    public Range(int start) {
        this.start = start;
        this.end = start;
    }

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) {
        visuals.run();
        ActionRange actionRange = new ActionRange(start, end);
        if (actionRange.start < actionRange.end) {
            logger.info(actionRange.toString());
        }
        return actionRange;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors) {
        script.actions.validate(script, action, new ActionRange(start, end),
                validationErrors);
    }
}
