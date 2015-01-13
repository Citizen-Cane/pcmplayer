package pcm.state.Interactions;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Return implements Interaction {

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            TeaseScript teaseScript) {
        if (visuals != null) {
            visuals.run();
        }
        teaseScript.completeAll();
        ActionRange range = script.stack.pop();
        TeaseLib.log("Return: " + range.toString());
        return range;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        // Nothing to do since the actual range is a runtime value
    }
}
