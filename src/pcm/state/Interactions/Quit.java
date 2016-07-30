package pcm.state.Interactions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import teaselib.ScriptFunction;

public class Quit implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(Quit.class);

    private Quit() {
    }

    public static final Quit instance = new Quit();

    @Override
    public ActionRange getRange(Script script, Action action,
            ScriptFunction visuals, Player player) {
        logger.info(getClass().getSimpleName());
        visuals.run();
        return null;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors) {
    }
}
