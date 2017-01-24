package pcm.state.Interactions;

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
import teaselib.ScriptFunction;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.speechrecognition.SpeechRecognition;

public class Break extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Break.class);

    public static final String SuppressStackCorrectionOnBreak = "suppressStackCorrectionOnBreak";

    private final ActionRange actionRange;
    private final Map<Statement, ActionRange> choiceRanges;
    private final boolean supressStackCorrectionOnBreak;

    public Break(ActionRange actionRange,
            Map<Statement, ActionRange> choiceRanges,
            boolean supressStackCorrectionOnBreak) {
        this.actionRange = actionRange;
        this.choiceRanges = choiceRanges;
        this.supressStackCorrectionOnBreak = supressStackCorrectionOnBreak;
    }

    @Override
    public ActionRange getRange(final Script script, final Action action,
            Runnable visuals, final Player player)
            throws ScriptExecutionException {
        int stackMemento = script.stack.size();
        visuals.run();
        List<String> choices = new ArrayList<String>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<ActionRange>(
                choiceRanges.size());
        for (Statement key : choiceRanges.keySet()) {
            choices.add(action.getResponseText(key, script));
            ranges.add(choiceRanges.get(key));
        }
        ScriptFunction playRange = new ScriptFunction() {
            @Override
            public void run() {
                try {
                    player.range = rangeProvider.getRange(script, action,
                            NoVisuals, player);
                    player.play(actionRange);
                    SpeechRecognition.completeSpeechRecognitionInProgress();
                } catch (ScriptInterruptedException e) {
                    // Must be forwarded to script function task
                    // in order to clean up
                    throw e;
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
                return;
            }
        };
        String result = player.reply(playRange, getConfidence(action).higher(),
                choices);
        if (result == ScriptFunction.Timeout) {
            return player.range;
        } else {
            if (!supressStackCorrectionOnBreak) {
                restoreStack(script, stackMemento);
            }
            int index = choices.indexOf(result);
            return ranges.get(index);
        }
    }

    private static void restoreStack(final Script script, int stackMemento) {
        while (script.stack.size() > stackMemento) {
            script.stack.pop();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getClass().getSimpleName() + ": ");
        for (Statement statement : choiceRanges.keySet()) {
            s.append(statement.toString() + "="
                    + choiceRanges.get(statement).toString());
        }
        return s.toString();
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            for (Statement key : choiceRanges.keySet()) {
                action.getResponseText(key, script);
            }
        } catch (ScriptExecutionException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
        script.actions.validate(script, action, actionRange, validationErrors);
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action, choiceRanges.get(statement), validationErrors);
        }
        if (!supressStackCorrectionOnBreak) {
            validateThatBreakPlayRangeDoesntAlterStack(script,
                    script.actions.getAll(actionRange), validationErrors);
        }
    }

    private static void validateThatBreakPlayRangeDoesntAlterStack(
            Script script, List<Action> actions,
            List<ValidationIssue> validationErrors) {
        for (Action action : actions) {
            if (action.interaction instanceof GoSub
                    || action.interaction instanceof Return) {
                validationErrors.add(new ValidationIssue(action,
                        action.interaction.getClass().getSimpleName()
                                + " may not be called in break play range",
                        script));
            }
        }
    }
}
