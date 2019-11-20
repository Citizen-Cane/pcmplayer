package pcm.state.interactions;

import java.util.Collections;
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
    public Action getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        if (!script.stack.isEmpty()) {
            Action next = script.stack.pop();
            logger.info("{}", next != null ? next : "end of script");
            return next;
        } else {
            throw new ScriptExecutionException(script, action, "Stack empty");
        }
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        // Nothing to do since the actual range is a runtime value
    }

    @Override
    public List<ActionRange> coverage() {
        return Collections.emptyList();
    }

}
