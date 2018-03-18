package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.visuals.Timeout;
import teaselib.ScriptFunction;
import teaselib.core.speechrecognition.SpeechRecognition.TimeoutBehavior;

public class Stop extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Stop.class);

    private final Map<Statement, ActionRange> choiceRanges;
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
        this.choiceRanges = choiceRanges;
        this.timeoutType = timeoutType;
        this.timeoutBehavior = timeoutBehavior;
    }

    @Override
    public ActionRange getRange(final Player player, Script script, final Action action, final Runnable visuals)
            throws ScriptExecutionException {
        logger.info("{}", this);
        List<String> choices = new ArrayList<>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<>(choiceRanges.size());
        for (Statement key : choiceRanges.keySet()) {
            choices.add(action.getResponseText(key, script));
            ranges.add(choiceRanges.get(key));
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

        String result = player.reply(timeoutFunction, choices);
        if (result != ScriptFunction.Timeout) {
            int index = choices.indexOf(result);
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getClass().getSimpleName() + ": ");
        for (Statement statement : choiceRanges.keySet()) {
            final ActionRange actionRange = choiceRanges.get(statement);
            s.append(statement.toString() + "=" + actionRange.toString() + " ");
        }
        return s.toString();
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            for (Statement key : choiceRanges.keySet()) {
                action.getResponseText(key, script);
            }
        } catch (Exception e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action, choiceRanges.get(statement), validationErrors);
        }

        super.validate(script, action, validationErrors);
    }
}
