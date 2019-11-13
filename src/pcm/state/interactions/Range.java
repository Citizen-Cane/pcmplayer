package pcm.state.interactions;

import java.util.Collections;
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

    public ActionRange getRange() {
        return new ActionRange(start, end);
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals) {
        visuals.run();
        ActionRange actionRange = new ActionRange(start, end);
        logger.info("{}", actionRange);
        return actionRange;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        script.actions.validate(script, action, new ActionRange(start, end), validationErrors);
    }

    @Override
    public List<ActionRange> coverage() {
        return Collections.singletonList(new ActionRange(start, end));
    }

}
