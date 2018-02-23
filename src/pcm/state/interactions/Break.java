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
import teaselib.ScriptFunction;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.speechrecognition.SpeechRecognition;

/**
 * Displays prompts while playing a range. THe following should be considered when using this statement:
 * <li>The stack is saved and restored, allowing for leaving the play range by interrupting the script function.
 * <li>All gosub/return pairs must be contained in the play range. This condition can't be validated, so be careful.
 * <li>If the break range is left by interrupting the script function within a gosub-statement, the stack is restored,
 * and execution continues at the exit range.
 * <li>In the valid case that the break range is executed as part of a gosub statement, and the play range contains a
 * return statement that doesn't result in exiting the play range, the "suppressStackCorrectionOnBreak" keyword must be
 * used to ensure a correct stack.
 * 
 * @author Citizen-Cane
 *
 */
public class Break extends AbstractInteractionWithRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Break.class);

    public static final String SuppressStackCorrectionOnBreak = "suppressStackCorrectionOnBreak";

    private final ActionRange breakRange;
    private final Map<Statement, ActionRange> choiceRanges;
    private final boolean supressStackCorrectionOnBreak;

    public Break(ActionRange actionRange, Map<Statement, ActionRange> choiceRanges,
            boolean supressStackCorrectionOnBreak) {
        this.breakRange = actionRange;
        this.choiceRanges = choiceRanges;
        this.supressStackCorrectionOnBreak = supressStackCorrectionOnBreak;
    }

    @Override
    public ActionRange getRange(final Player player, final Script script, final Action action, Runnable visuals)
            throws ScriptExecutionException {
        int stackMemento = script.stack.size();
        visuals.run();
        List<String> choices = new ArrayList<>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<>(choiceRanges.size());
        for (Statement key : choiceRanges.keySet()) {
            choices.add(action.getResponseText(key, script));
            ranges.add(choiceRanges.get(key));
        }
        ScriptFunction playRange = new ScriptFunction() {
            @Override
            public void run() {
                try {
                    player.range = rangeProvider.getRange(player, script, action, NoVisuals);
                    player.playRange(breakRange);
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
        String result = player.reply(playRange, getConfidence(action).higher(), choices);
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
            s.append(statement.toString() + "=" + choiceRanges.get(statement).toString());
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
        script.actions.validate(script, action, breakRange, validationErrors);
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action, choiceRanges.get(statement), validationErrors);
        }

        super.validate(script, action, validationErrors);
    }
}
