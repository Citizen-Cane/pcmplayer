package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.AskItem;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Command;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.State;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Ask implements Command, Interaction, NeedsRangeProvider {
    private final int start;
    private final int end;

    private State state = null;

    private Interaction rangeProvider = null;

    public Ask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void execute(State state) {
        this.state = state;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals,
            TeaseScript teaseScript) throws ScriptExecutionError {
        List<Boolean> values = new ArrayList<>();
        List<String> choices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<Integer, AskItem> askItems = script.askItems;
        String message = null;
        for (int i = start; i <= end; i++) {
            Integer index = new Integer(i);
            if (askItems.containsKey(index)) {
                AskItem askItem = askItems.get(index);
                if (askItem.action == 0) {
                    message = askItem.message;
                } else {
                    Boolean value = state.get(askItem.action) == State.SET ? Boolean.TRUE
                            : Boolean.FALSE;
                    values.add(value);
                    choices.add(askItem.message);
                    indices.add(new Integer(askItem.action));
                }
            }
        }
        TeaseLib.log(getClass().getSimpleName() + " " + choices.toString());
        visuals.run();
        // Don't wait, display checkboxes while displaying the message
        List<Boolean> results;
        while ((results = teaseScript.showCheckboxes(message, choices, values)) == null)
            ;
        for (int i = 0; i < indices.size(); i++) {
            if (results.get(i) == true) {
                state.set(indices.get(i));
            } else {
                state.unset(indices.get(i));
            }
        }
        return rangeProvider.getRange(script, action, null, teaseScript);
    }

    @Override
    public void setRangeProvider(Interaction rangeProvider) {
        this.rangeProvider = rangeProvider;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) throws ParseError {
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
