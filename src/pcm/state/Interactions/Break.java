package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import teaselib.ScriptInterruptedException;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Break implements Interaction, NeedsRangeProvider {
    private final ActionRange actionRange;
    private ActionRange stopRange = null;
    private ActionRange cumRange = null;
    private Interaction rangeProvider = null;

    public Break(ActionRange actionRange, ActionRange stopRange) {
        this.actionRange = actionRange;
        this.stopRange = stopRange;
        this.cumRange = null;
    }

    public Break(ActionRange actionRange, Map<String, ActionRange> choices) {
        this.actionRange = actionRange;
        for (String keyword : choices.keySet()) {
            ActionRange range = choices.get(keyword);
            if (keyword.toLowerCase().equals("cum")) {
                this.cumRange = range;
            } else if (keyword.toLowerCase().equals("stop")) {
                this.stopRange = range;
            }
        }
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            TeaseScript teaseScript) throws ScriptExecutionError {
        // First run the visuals of this actions (likely not any, but who knows)
        visuals.run();
        // TODO change TeaseScript to Player since it's just that
        Player player = (Player) teaseScript;
        List<String> choices = new ArrayList<>(2);
        String cumText = cumRange != null ? action.getResponseText(
                Statement.CumText, script) : null;
        if (cumRange != null) {
            choices.add(cumText);
        }
        String stopText = action.getResponseText(Statement.StopText, script);
        choices.add(stopText);
        String result = teaseScript.choose(
                () -> {
                    try {
                        player.range = rangeProvider.getRange(script, action,
                                null, teaseScript);
                        player.play(actionRange);
                    } catch (ScriptInterruptedException e) {
                        // Expected
                    } catch (Throwable t) {
                        TeaseLib.log(this, t);
                    }
                }, choices);
        if (result == cumText) {
            TeaseLib.log("-> break:cum");
            teaseScript.teaseLib.host.stopSounds();
            return cumRange;
        } else if (result == stopText) {
            TeaseLib.log("-> break:stop");
            teaseScript.teaseLib.host.stopSounds();
            return stopRange;
        } else {
            return player.range;
        }
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        this.rangeProvider = rangeProvider;
    }

    @Override
    public String toString() {
        String s = getClass().getSimpleName() + ": " + actionRange.toString()
                + " to  stop: " + stopRange.toString();
        if (cumRange != null) {
            s = s + ", to  cum: " + cumRange.toString();
        }
        return s;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError {
        script.actions.validate(action, actionRange, validationErrors);
        if (stopRange != null) {
            script.actions.validate(action, stopRange, validationErrors);
        }
        if (cumRange != null) {
            script.actions.validate(action, cumRange, validationErrors);
        }
    }
}
