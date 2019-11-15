package pcm.state.interactions;

import java.util.ArrayList;
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
import pcm.state.Interaction;

/**
 * @author Citizen-Cane
 *
 *         Calls a sub program specified by the range argument after rendering the visuals of the action.
 * 
 */
public class GoSub extends AbstractInteraction {
    private static final Logger logger = LoggerFactory.getLogger(GoSub.class);

    private final ActionRange range;

    public GoSub(ActionRange range) {
        this.range = range;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        logger.info("{}", range);
        visuals.run();
        script.stack.push(rangeProvider.getRange(player, script, action, NoVisuals));
        return range;
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        if (rangeProvider instanceof Range || rangeProvider instanceof GoSub || rangeProvider instanceof Return
                || rangeProvider instanceof LoadSbd) {
            super.setRangeProvider(rangeProvider);
        } else {
            throw new IllegalArgumentException("Wrong interaction sequence: " + rangeProvider.getClass().getSimpleName()
                    + " can only be executed first");
        }
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        range.validate();
        script.actions.validate(script, action, range, validationErrors);
        super.validate(script, action, validationErrors);
    }

    @Override
    public List<ActionRange> coverage() {
        List<ActionRange> rangeProviderCoverage = super.coverage();
        List<ActionRange> coverage = new ArrayList<>(1 + rangeProviderCoverage.size());
        coverage.add(range);
        coverage.addAll(rangeProviderCoverage);
        return coverage;
    }

}
