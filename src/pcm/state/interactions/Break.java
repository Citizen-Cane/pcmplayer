package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import teaselib.Answer;
import teaselib.Answers;
import teaselib.ScriptFunction;
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
    public Action getRange(final Player player, final Script script, final Action action, Runnable visuals)
            throws ScriptExecutionException {
        int stackMemento = script.stack.size();
        visuals.run();
        Answers answers = new Answers(choiceRanges.size());
        List<ActionRange> ranges = new ArrayList<>(choiceRanges.size());
        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            answers.add(Answer.resume(action.getResponseText(entry.getKey(), script)));
            ranges.add(entry.getValue());
        }

        player.action = rangeProvider.getRange(player, script, action, NoVisuals);
        ScriptFunction playRange = new ScriptFunction(() -> {
            try {
                player.playRange(breakRange);
            } catch (ScriptExecutionException e) {
                throw ExceptionUtil.asRuntimeException(e);
            }
            player.scriptRenderer.audioSync.completeSpeechRecognition();
        });

        Answer result = player.reply(playRange, answers);
        if (result.equals(Answer.Timeout)) {
            return player.action;
        } else {
            if (!supressStackCorrectionOnBreak) {
                restoreStack(script, stackMemento);
            }
            int index = answers.indexOf(result);
            return player.getAction(ranges.get(index));
        }
    }

    private static void restoreStack(final Script script, int stackMemento) {
        while (script.stack.size() > stackMemento) {
            script.stack.pop();
        }
    }

}
