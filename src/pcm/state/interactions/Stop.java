package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import teaselib.Answers;
import teaselib.ScriptFunction;
import teaselib.core.speechrecognition.SpeechRecognition.TimeoutBehavior;

public class Stop extends AbstractBreakInteraction {
    private static final Logger logger = LoggerFactory.getLogger(Stop.class);

    private final TimeoutType timeoutType;
    private final TimeoutBehavior timeoutBehavior;

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

    public Stop(Map<Statement, ActionRange> choiceRanges, TimeoutType timeoutType, TimeoutBehavior timeoutBehavior) {
        super(choiceRanges);
        this.timeoutType = timeoutType;
        this.timeoutBehavior = timeoutBehavior;
    }

    @Override
    public ActionRange getRange(final Player player, Script script, final Action action, final Runnable visuals)
            throws ScriptExecutionException {
        logger.info("{}", this);

        Answers answers = new Answers(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<>(choiceRanges.size());
        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            String choice = action.getResponseText(entry.getKey(), script);
            if (entry.getKey() == Statement.YesText) {
                answers.add(Answer.yes(choice));
            } else if (entry.getKey() == Statement.NoText) {
                answers.add(Answer.no(choice));
            } else {
                answers.add(Answer.resume(choice));
            }
            ranges.add(entry.getValue());
        }

        ScriptFunction timeoutFunction;
        // If the reply requires confirmation, then it's treated like a normal reply,
        // and the prompt appears after all visuals have been rendered their mandatory part
        if (timeoutType != TimeoutType.Terminate) {
            // Render visuals directly, then display prompt with predefined timeout function
            visuals.run();
            Timeout timeout = (Timeout) action.visuals.get(Statement.Delay);
            if (timeoutType == TimeoutType.AutoConfirm) {
                timeoutFunction = timeoutWithAutoConfirmation(player, timeout);
            } else {
                timeoutFunction = timeoutWithConfirmation(player, timeout);
            }
        } else {
            timeoutFunction = displayPromptTogetherWithScriptFunction(player, visuals);
        }

        Answer result = player.reply(timeoutFunction, answers);
        if (result != ScriptFunction.Timeout) {
            int index = answers.indexOf(result);
            logger.info("-> {}", result);
            return ranges.get(index);
        } else {
            return rangeProvider.getRange(player, script, action, NoVisuals);
        }
    }

    private ScriptFunction timeoutWithAutoConfirmation(final Player player, Timeout timeout) {
        return player.timeoutWithAutoConfirmation(timeout.duration, timeoutBehavior);
    }

    private ScriptFunction timeoutWithConfirmation(final Player player, Timeout timeout) {
        return player.timeoutWithConfirmation(timeout.duration, timeoutBehavior);
    }

    private static ScriptFunction displayPromptTogetherWithScriptFunction(final Player player, final Runnable visuals) {
        return new ScriptFunction(() -> {
            visuals.run();
            player.completeMandatory();
        });
    }
}
