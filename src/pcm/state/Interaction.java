package pcm.state;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;

public interface Interaction {
    /**
     * Return a range to be executed next.
     * 
     * @param script
     *            The script.
     * @param action
     *            The current action.
     * @param visuals
     *            The visuals to be rendered before returning the range.
     *            Interactions without interaction (ouch) should call
     *            completeAll() before returning, in order to allow the visuals
     *            to complete, or completeMandatory (explicitly or implicitly)
     * @param player
     *            The TeaseScript instance for rendering the interaction
     * 
     * @return
     * @throws ScriptExecutionError
     */
    ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) throws ScriptExecutionError;

    void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError;

    public interface NeedsRangeProvider {
        public void setRangeProvider(Interaction rangeProvider);
    }
}
