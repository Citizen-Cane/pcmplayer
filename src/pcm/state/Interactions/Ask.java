package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
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
            Player player) throws ScriptExecutionError {
        List<Boolean> values = new ArrayList<Boolean>();
        List<String> choices = new ArrayList<String>();
        List<Integer> indices = new ArrayList<Integer>();
        Map<Integer, AskItem> askItems = script.askItems;
        String message = null;
        for (int i = start; i <= end; i++) {
            Integer index = new Integer(i);
            if (askItems.containsKey(index)) {
                AskItem askItem = askItems.get(index);
                if (askItem.action == 0) {
                    message = askItem.message;
                } else {
                    int condition = askItem.condition;
                    if (condition == AskItem.ALWAYS
                            || state.get(condition).equals(State.SET)) {
                        Boolean value = state.get(askItem.action) == State.SET ? Boolean.TRUE
                                : Boolean.FALSE;
                        values.add(value);
                        choices.add(askItem.message);
                        indices.add(new Integer(askItem.action));
                    }
                }
            }
        }
        TeaseLib.log(getClass().getSimpleName() + " " + choices.toString());
        visuals.run();
        // Don't wait, display checkboxes while displaying the message
        List<Boolean> results;
        while ((results = player.showCheckboxes(message, choices, values)) == null)
            ;
        for (int i = 0; i < indices.size(); i++) {
            if (results.get(i) == true) {
                state.set(indices.get(i));
            } else {
                state.unset(indices.get(i));
            }
        }
        return rangeProvider.getRange(script, action, null, player);
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
