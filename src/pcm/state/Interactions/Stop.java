package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ScriptParsingException;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.visuals.Timeout;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.speechrecognition.SpeechRecognition.TimeoutBehavior;

public class Stop implements Interaction, NeedsRangeProvider {

    private final Map<Statement, ActionRange> choiceRanges;
    private final TimeoutType timeoutType;
    private final TimeoutBehavior timeoutBehavior;

    private Interaction rangeProvider = null;

    public enum TimeoutType {
        /**
         * Show buttons right at the start (in contrast to normal replies.
         * Dismiss prompts when the duration is over.
         */
        Terminate,
        /**
         * Show prompts as if this was a normal button, and wait for input
         * according to the timeout behavior.
         */
        Confirm,
        /**
         * Show prompts as in normal replies, but dismiss buttons when the
         * duration is over.
         */
        AutoConfirm
    }

    public Stop(Map<Statement, ActionRange> choiceRanges,
            TimeoutType timeoutType, TimeoutBehavior timeoutBehavior) {
        this.choiceRanges = choiceRanges;
        this.timeoutType = timeoutType;
        this.timeoutBehavior = timeoutBehavior;
    }

    @Override
    public ActionRange getRange(Script script, final Action action,
            final ScriptFunction visuals, final Player player)
            throws ScriptExecutionException {
        TeaseLib.instance().log
                .info(getClass().getSimpleName() + " " + toString());
        List<String> choices = new ArrayList<String>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<ActionRange>(
                choiceRanges.size());
        for (Statement key : choiceRanges.keySet()) {
            choices.add(action.getResponseText(key, script));
            ranges.add(choiceRanges.get(key));
        }
        // If the reply requires confirmation, then it's treated like a normal
        // reply, and the buttons appear after all visuals have been rendered
        final boolean normalPrompt = timeoutType != TimeoutType.Terminate;
        if (normalPrompt) {
            // Pretend to be a normal button and show after completing mandatory
            // visuals
            visuals.run();
            // Now we don't want to wait for the delay to complete, we want to
            // display the buttons after the message has been rendered, so let's
            // special-case some more and ignore the delay renderer
            player.completeMandatory();
        }
        ScriptFunction displayVisualsAndTimeout = new ScriptFunction() {
            @Override
            public void run() {
                final ScriptFunction timeoutFunction;
                if (normalPrompt) {
                    // Visuals are rendered in the main loop, and the timeout
                    // visual doesn't render a delay, allowing us to query and
                    // wait the timeout duration in the script function
                    Timeout timeout = (Timeout) action.visuals
                            .get(Statement.Delay);
                    if (timeoutType == TimeoutType.AutoConfirm) {
                        timeoutFunction = player.timeoutWithAutoConfirmation(
                                timeout.duration, timeoutBehavior);
                    } else {
                        timeoutFunction = player.timeoutWithConfirmation(
                                timeout.duration, timeoutBehavior);
                    }
                } else {
                    visuals.run();
                    // Complete visuals and full delay
                    player.completeMandatory();
                    // At this point we're delayed already, since we've added a
                    // delay renderer to the visuals of the action when parsing
                    // the script, so we can just finish the script function
                    // with the default timeout script function and complete
                    // speech recognition with the default timeout function
                    timeoutFunction = player.timeout(0, timeoutBehavior);
                }
                try {
                    timeoutFunction.run();
                } catch (ScriptInterruptedException e) {
                    // pass the result to the outer script function
                    result = timeoutFunction.result;
                    throw e;
                }
            }

        };
        String result = player.reply(displayVisualsAndTimeout, choices);
        if (result != ScriptFunction.Timeout) {
            int index = choices.indexOf(result);
            TeaseLib.instance().log.info("-> " + result);
            return ranges.get(index);
        } else {
            return rangeProvider.getRange(script, action, null, player);
        }
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        this.rangeProvider = rangeProvider;
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
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors) throws ScriptParsingException {
        try {
            for (Statement key : choiceRanges.keySet()) {
                action.getResponseText(key, script);
            }
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action, choiceRanges.get(statement),
                    validationErrors);
        }
    }
}
