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
import teaselib.ScriptFunction;
import teaselib.TeaseLib;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.speechrecognition.SpeechRecognition;

public class Break implements Interaction, NeedsRangeProvider {
    private final ActionRange actionRange;
    private final Map<Statement, ActionRange> choiceRanges;

    private Interaction rangeProvider = null;

    public Break(ActionRange actionRange,
            Map<Statement, ActionRange> choiceRanges) {
        this.actionRange = actionRange;
        this.choiceRanges = choiceRanges;
    }

    @Override
    public ActionRange getRange(final Script script, final Action action,
            ScriptFunction visuals, final Player player)
            throws ScriptExecutionError {
        // First run the visuals of this action
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
                    player.range = rangeProvider.getRange(script, action, null,
                            player);
                    player.play(actionRange);
                    SpeechRecognition.completeSpeechRecognitionInProgress();
                } catch (ScriptInterruptedException e) {
                    // Must be forwarded to script function task
                    // in order to clean up
                    throw e;
                } catch (Throwable t) {
                    TeaseLib.instance().log.error(this, t);
                }
                return;
            }
        };
        String result = player.reply(playRange, choices);
        if (result != ScriptFunction.Timeout) {
            int index = choices.indexOf(result);
            return ranges.get(index);
        } else {
            return player.range;
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
                    + choiceRanges.get(statement).toString());
        }
        return s.toString();
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError {
        script.actions.validate(script, action, actionRange, validationErrors);
        for (Statement statement : choiceRanges.keySet()) {
            script.actions.validate(script, action,
                    choiceRanges.get(statement), validationErrors);
        }
    }
}
