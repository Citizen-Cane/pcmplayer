package pcm.state.Interactions;

import java.util.List;

import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;

public abstract class AbstractInteractionWithRangeProvider extends AbstractInteraction implements NeedsRangeProvider {

    protected Interaction rangeProvider = null;

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        if (this.rangeProvider != null) {
            if (this.rangeProvider instanceof NeedsRangeProvider) {
                ((NeedsRangeProvider) this.rangeProvider).setRangeProvider(rangeProvider);
            } else {
                throw new IllegalStateException(rangeProvider.toString());
            }
        } else {
            this.rangeProvider = rangeProvider;
        }
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        } else {
            validationErrors.add(new ValidationIssue(action, "Range provider missing", script));
        }
    }
}
