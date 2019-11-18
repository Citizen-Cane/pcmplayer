package pcm.state.interactions;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import teaselib.Message;

public class Quit implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(Quit.class);

    private Quit() {
    }

    public static final Quit instance = new Quit();

    @Override
    public Action getRange(Player player, Script script, Action action, Runnable visuals) {
        logger.info(getClass().getSimpleName());
        visuals.run();

        player.completeAll();
        player.setImage(Message.NoImage);
        player.show("");

        return Player.EndAction;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) { // Ignore
    }

    @Override
    public List<ActionRange> coverage() {
        return Collections.emptyList();
    }

}
