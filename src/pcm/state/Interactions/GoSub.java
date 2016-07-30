package pcm.state.Interactions;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ScriptParsingException;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;

/**
 * @author someone
 *
 *         Calls a sub program specified by the range argument after rendering
 *         the visuals of the action.
 * 
 */
public class GoSub implements Interaction, Interaction.NeedsRangeProvider {
    private Interaction rangeProvider = null;
    final private ActionRange range;

    public GoSub(ActionRange range) {
        this.range = range;
    }

    @Override
    public ActionRange getRange(Script script, Action action,
            ScriptFunction visuals, Player player) throws ScriptExecutionException {
        TeaseLib.instance().log.info("Gosub -> " + range.toString());
        visuals.run();
        script.stack.push(rangeProvider.getRange(script, action, null, player));
        return range;
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
            List<ValidationIssue> validationErrors) throws ScriptParsingException {
        range.validate();
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
