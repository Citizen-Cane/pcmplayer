package pcm.state.interactions;

import java.util.Map;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import teaselib.Answer;
import teaselib.ScriptFunction;
import teaselib.core.util.ExceptionUtil;

/**
 * Displays prompts while playing a range. The following should be considered when using this statement:
 * <li>The stack is saved and restored, for allowing to exit the play range by interrupting the script function.
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
    public Action getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        if (breakRange == player.playRange) {
            throw new ScriptExecutionException(script, action, "Trying to break the same range twice");
        }
        int stackMemento = script.stack.size();
        visuals.run();

        player.action = getNextAction(player, script, action);
        ScriptFunction playRange = getPlayRange(player);

        return performBreakRange(player, script, action, stackMemento, playRange);
    }

    protected Action getNextAction(Player player, Script script, Action action) throws ScriptExecutionException {
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }

    private ScriptFunction getPlayRange(final Player player) {
        return new ScriptFunction(() -> {
            try {
                player.playRange(breakRange);
            } catch (ScriptExecutionException e) {
                throw ExceptionUtil.asRuntimeException(e);
            }
            player.awaitAllCompleted();
        });
    }

    private Action performBreakRange(Player player, Script script, Action action, int stackMemento,
            ScriptFunction playRange) throws AllActionsSetException {
        Map<Answer, ActionRange> ranges = ranges(script, action);
        Answer result = player.reply(playRange, answers(ranges));
        if (result.equals(Answer.Timeout)) {
            return player.action;
        } else {
            if (!supressStackCorrectionOnBreak) {
                restoreStack(script, stackMemento);
            }
            return player.getAction(ranges.get(result));
        }
    }

    private static void restoreStack(Script script, int stackMemento) {
        while (script.stack.size() > stackMemento) {
            script.stack.pop();
        }
    }

}
