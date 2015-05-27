package pcm.state.Interactions;

import java.util.List;

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
import teaselib.TeaseLib;

public class Stop implements Interaction, NeedsRangeProvider {

    private final ActionRange stop;
    private Interaction rangeProvider = null;

    public Stop(ActionRange stop) {
        this.stop = stop;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) throws ScriptExecutionError {
        String stopText = action.getResponseText(Statement.StopText, script);
        TeaseLib.log(getClass().getSimpleName() + " " + toString());
        String result = player.reply(visuals, stopText);
        if (result == stopText) {
            TeaseLib.log("-> Stop");
            return stop;
        } else {
            return rangeProvider.getRange(script, action, null, player);
        }
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        this.rangeProvider = rangeProvider;
    }

    @Override
    public String toString() {
        return stop.toString();
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError {
        script.actions.validate(script, action, stop, validationErrors);
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
