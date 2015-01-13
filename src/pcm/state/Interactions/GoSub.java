package pcm.state.Interactions;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

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
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            TeaseScript teaseScript) throws ScriptExecutionError {
        TeaseLib.log("Gosub -> " + range.toString());
        script.stack.push(rangeProvider.getRange(script, action, null,
                teaseScript));
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
            List<ValidationError> validationErrors) throws ParseError {
        range.validate();
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
