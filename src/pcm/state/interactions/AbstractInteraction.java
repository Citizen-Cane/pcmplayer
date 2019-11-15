package pcm.state.interactions;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;

public abstract class AbstractInteraction implements Interaction, NeedsRangeProvider {

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

    @Override
    public List<ActionRange> coverage() {
        return rangeProvider.coverage();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(getClass().getSimpleName());
        if (rangeProvider != null) {
            string.append("->");
            string.append(rangeProvider.toString());
        }
        return string.toString();
    }

}
