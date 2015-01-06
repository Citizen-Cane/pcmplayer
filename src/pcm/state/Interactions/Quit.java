package pcm.state.Interactions;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Quit implements Interaction {

    private Quit() {
    }

    public static final Quit instance = new Quit();

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            TeaseScript teaseScript) {
        TeaseLib.log(getClass().getSimpleName());
        visuals.run();
        teaseScript.completeAll();
        return null;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
    }
}
