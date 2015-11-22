package pcm.state.Interactions;

import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;

public class Return implements Interaction {

    @Override
    public ActionRange getRange(Script script, Action action,
            ScriptFunction visuals, Player player) {
        if (visuals != null) {
            visuals.run();
        }
        if (script.stack.size() > 0) {
            ActionRange range = script.stack.pop();
            TeaseLib.instance().log.info("Return: "
                    + (range != null ? range.toString() : "end of script"));
            return range;
        } else {
            TeaseLib.instance().log.info("Return: stack empty");
            return null;
        }
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        // Nothing to do since the actual range is a runtime value
    }
}
