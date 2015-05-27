package pcm.state.Interactions;

import java.util.ArrayList;
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
import teaselib.TeaseLib;

public class Pause implements Interaction, Interaction.NeedsRangeProvider {
    private Interaction rangeProvider = null;

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) throws ScriptExecutionError {
        String resume = action.getResponseText(Statement.ResumeText, script);
        TeaseLib.log("Pause: " + resume);
        List<String> choices = new ArrayList<String>(1);
        choices.add(resume);
        visuals.run();
        player.reply(choices);
        return rangeProvider.getRange(script, action, null, player);
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
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
