package pcm.state.interactions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.state.visuals.Timeout;
import teaselib.Answer;
import teaselib.ScriptFunction;
import teaselib.core.speechrecognition.TimeoutBehavior;

/**
 * THe Stop statement executes the current action as a timed script function.
 * 
 * @author Citizen-Cane
 *
 */
public abstract class Stop extends AbstractBreakInteraction {
    private static final Logger logger = LoggerFactory.getLogger(Stop.class);

    final TimeoutBehavior timeoutBehavior;

    public Stop(Map<Statement, ActionRange> choiceRanges, TimeoutBehavior timeoutBehavior) {
        super(choiceRanges);
        this.timeoutBehavior = timeoutBehavior;
    }

    public enum TimeoutType {
        /**
         * Show buttons right at the start (in contrast to normal replies. Dismiss prompts when the duration is over.
         */
        Terminate,
        /**
         * Show prompts as if this was a normal button, and wait for input according to the timeout behavior.
         */
        Confirm,
        /**
         * Show prompts as in normal replies, but dismiss buttons when the duration is over.
         */
        AutoConfirm
    }

    /**
     * Render visuals directly, then display prompt with predefined timeout function.
     * 
     * @author Citizen-Cane
     *
     */
    public static class Confirmative extends Stop {
        private final TimeoutType timeoutType;

        public Confirmative(Map<Statement, ActionRange> choiceRanges, TimeoutType timeoutType,
                TimeoutBehavior timeoutBehavior) {
            super(choiceRanges, timeoutBehavior);
            this.timeoutType = timeoutType;
        }

        @Override
        public Action getRange(Player player, Script script, Action action, Runnable visuals)
                throws ScriptExecutionException {
            logger.info("{}", this);

            visuals.run();

            ScriptFunction timeoutFunction;
            Timeout timeout = (Timeout) action.visuals.get(Statement.Delay);
            if (timeoutType == TimeoutType.AutoConfirm) {
                timeoutFunction = timeoutWithAutoConfirmation(player, timeout);
            } else {
                timeoutFunction = timeoutWithConfirmation(player, timeout);
            }

            return nextAction(player, script, action, timeoutFunction);
        }

        private ScriptFunction timeoutWithAutoConfirmation(final Player player, Timeout timeout) {
            return player.timeoutWithAutoConfirmation(timeout.duration, timeoutBehavior);
        }

        private ScriptFunction timeoutWithConfirmation(final Player player, Timeout timeout) {
            return player.timeoutWithConfirmation(timeout.duration, timeoutBehavior);
        }
    }

    /**
     * Render visuals in a script function.
     * 
     * @author Citizen-Cane
     *
     */
    public static class Termimative extends Stop {
        public Termimative(Map<Statement, ActionRange> choiceRanges, TimeoutBehavior timeoutBehavior) {
            super(choiceRanges, timeoutBehavior);
        }

        @Override
        public Action getRange(Player player, Script script, Action action, Runnable visuals)
                throws ScriptExecutionException {
            logger.info("{}", this);

            ScriptFunction timeoutFunction = new ScriptFunction(() -> {
                visuals.run();
                player.awaitMandatoryCompleted();
            });

            return nextAction(player, script, action, timeoutFunction);
        }
    }

    Action nextAction(Player player, Script script, Action action, ScriptFunction timeoutFunction)
            throws ScriptExecutionException {
        Map<Answer, ActionRange> ranges = ranges(script, action);
        Answer result = player.reply(timeoutFunction, answers(ranges));
        if (result != Answer.Timeout) {
            logger.info("-> {}", result);
            return player.getAction(ranges.get(result));
        } else {
            return rangeProvider.getRange(player, script, action, NoVisuals);
        }
    }

}
