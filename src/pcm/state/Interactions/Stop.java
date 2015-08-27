package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.visuals.Timeout;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;
import teaselib.TeaseLib.Duration;
import teaselib.core.ScriptInterruptedException;

public class Stop implements Interaction, NeedsRangeProvider {

    private final Map<Statement, ActionRange> choiceRanges;
    private final TimeoutBehavior timeoutBehavior;

    private Interaction rangeProvider = null;

    public enum TimeoutBehavior {
        Terminate,
        Confirm
    }

    public Stop(Map<Statement, ActionRange> choiceRanges,
            TimeoutBehavior timeoutBehavior) {
        this.choiceRanges = choiceRanges;
        this.timeoutBehavior = timeoutBehavior;
    }

    @Override
    public ActionRange getRange(Script script, final Action action,
            final ScriptFunction visuals, final Player player)
            throws ScriptExecutionError {
        TeaseLib.log(getClass().getSimpleName() + " " + toString());
        List<String> choices = new ArrayList<String>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<ActionRange>(
                choiceRanges.size());
        for (Statement key : choiceRanges.keySet()) {
            choices.add(action.getResponseText(key, script));
            ranges.add(choiceRanges.get(key));
        }
        // If the reply requires confirmation, then it's treated like a normal
        // reply, and the buttons appear after all visuals have been rendered
        final boolean treatAsNormalReply = timeoutBehavior == TimeoutBehavior.Confirm;
        final Duration visualRenderDuration = player.duration();
        if (treatAsNormalReply) {
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
                if (treatAsNormalReply) {
                    // Visuals are rendered in the main loop, and timeout
                    // doesn't render a delay, allowing us to wait the timeout
                    // duration in the script function
                    Timeout timeout = (Timeout) action.visuals
                            .get(Statement.Delay);
                    long elapsedSeconds = visualRenderDuration.elapsedSeconds();
                    timeoutFunction = player
                            .timeoutWithConfirmation(timeout.duration
                                    - elapsedSeconds);
                } else {
                    visuals.run();
                    player.completeMandatory();
                    // At this point we're delayed already, since we've added a
                    // delay renderer to the visuals of the action when parsing
                    // the script, so we can just finish the script function
                    // with the default timeout script function and complete
                    // speech recognition with the default timeout function
                    timeoutFunction = player.timeout(0);
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
            TeaseLib.log("-> " + result);
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
            s.append(statement.toString() + "="
                    + choiceRanges.get(statement).toString() + " ");
        }
        return s.toString();
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError {
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action,
                    choiceRanges.get(statement), validationErrors);
        }
    }
}
