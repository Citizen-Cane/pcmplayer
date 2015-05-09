package pcm.state.Interactions;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;

public class Range implements Interaction {

    private final int start;
    private final int end;

    public Range(int start) {
        this.start = start;
        this.end = start;
    }

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) {
        if (visuals != null) {
            visuals.run();
        }
        player.completeAll();
        ActionRange actionRange = new ActionRange(start, end);
        TeaseLib.log(actionRange.toString());
        return actionRange;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        script.actions.validate(script, action, new ActionRange(start, end),
                validationErrors);
    }
}
