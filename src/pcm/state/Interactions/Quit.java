package pcm.state.Interactions;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;

public class Quit implements Interaction {

    private Quit() {
    }

    public static final Quit instance = new Quit();

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) {
        TeaseLib.log(getClass().getSimpleName());
        visuals.run();
        player.completeAll();
        return null;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
    }
}
