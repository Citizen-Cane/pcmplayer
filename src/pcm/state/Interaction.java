package pcm.state;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;

public interface Interaction {
    static final Runnable NoVisuals = new Runnable() {
        @Override
        public void run() {
        }
    };

    /**
     * Return a range to be executed next.
     * 
     * @param player
     *            The TeaseScript instance for rendering the interaction
     * @param script
     *            The script.
     * @param action
     *            The current action.
     * @param visuals
     *            The visuals to be rendered before returning the range. Interactions without interaction (ouch) should
     *            call completeAll() before returning, in order to allow the visuals to complete, or completeMandatory
     *            (explicitly or implicitly)
     * 
     * @return
     * @throws ScriptExecutionException
     */
    ActionRange getRange(Player player, Script script, Action action, Runnable visuals) throws ScriptExecutionException;

    void validate(Script script, Action action, List<ValidationIssue> validationErrors) throws ScriptParsingException;

    public interface NeedsRangeProvider {
        public void setRangeProvider(Interaction rangeProvider);
    }

    List<ActionRange> coverage();
}
