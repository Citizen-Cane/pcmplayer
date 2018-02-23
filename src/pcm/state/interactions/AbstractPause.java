package pcm.state.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import teaselib.Answer;

public class AbstractPause extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractPause.class);

    public final Answer answer;

    public AbstractPause(Answer answer) {
        this.answer = answer;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();

        logger.info(getClass().getSimpleName() + ": " + answer);
        player.reply(getConfidence(action), answer);
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }
}
