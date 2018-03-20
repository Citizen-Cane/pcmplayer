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
import teaselib.ScriptFunction;
import teaselib.core.speechrecognition.SpeechRecognition;
import teaselib.core.util.ExceptionUtil;

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
public class Break extends AbstractBreakInteraction {
    private static final Logger logger = LoggerFactory.getLogger(Break.class);

    public static final String SuppressStackCorrectionOnBreak = "suppressStackCorrectionOnBreak";

    private final ActionRange breakRange;
    private final boolean supressStackCorrectionOnBreak;

    public Break(ActionRange actionRange, Map<Statement, ActionRange> choiceRanges,
            boolean supressStackCorrectionOnBreak) {
        super(choiceRanges);
        this.breakRange = actionRange;
        this.supressStackCorrectionOnBreak = supressStackCorrectionOnBreak;
    }

    @Override
    public ActionRange getRange(final Player player, final Script script, final Action action, Runnable visuals)
            throws ScriptExecutionException {
        int stackMemento = script.stack.size();
        visuals.run();
        List<String> choices = new ArrayList<>(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<>(choiceRanges.size());
        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            choices.add(action.getResponseText(entry.getKey(), script));
            ranges.add(entry.getValue());
        }

        ScriptFunction playRange = new ScriptFunction(() -> {
            try {
                player.range = rangeProvider.getRange(player, script, action, NoVisuals);
                player.playRange(breakRange);
            } catch (ScriptExecutionException e) {
                throw ExceptionUtil.asRuntimeException(e);
            }
            SpeechRecognition.completeSpeechRecognitionInProgress();
        });

        String result = player.reply(playRange, choices);
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
}
