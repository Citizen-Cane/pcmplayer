package pcm.state.Interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class Return implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(Return.class);

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            Player player) throws ScriptExecutionException {
        visuals.run();
        if (script.stack.size() > 0) {
            ActionRange range = script.stack.pop();
            logger.info(range != null ? range.toString() : "end of script");
            return range;
        } else {
            throw new ScriptExecutionException(action, "Stack empty", script);
        }
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors) {
        // Nothing to do since the actual range is a runtime value
    }
}
