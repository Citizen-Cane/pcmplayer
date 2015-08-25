package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
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
import pcm.state.MappedState;
import pcm.state.State;
import pcm.state.Visual;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;
import teaselib.Toys;
import teaselib.util.Item;
import teaselib.util.Items;

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
    public ActionRange getRange(Script script, Action action,
            ScriptFunction visuals, Player player) throws ScriptExecutionError {
        List<Boolean> values = new ArrayList<Boolean>();
        List<String> choices = new ArrayList<String>();
        List<Integer> indices = new ArrayList<Integer>();
        Map<Integer, AskItem> askItems = script.askItems;
        String title = null;
        for (int i = start; i <= end; i++) {
            Integer index = new Integer(i);
            if (askItems.containsKey(index)) {
                AskItem askItem = askItems.get(index);
                if (askItem.action == 0) {
                    title = askItem.title;
                } else {
                    int condition = askItem.condition;
                    if (condition == AskItem.ALWAYS
                            || state.get(condition).equals(State.SET)) {
                        Boolean value = state.get(askItem.action) == State.SET ? Boolean.TRUE
                                : Boolean.FALSE;
                        values.add(value);
                        choices.add(askItem.title);
                        indices.add(new Integer(askItem.action));
                    }
                }
            }
        }
        TeaseLib.log(getClass().getSimpleName() + " " + choices.toString());
        visuals.run();
        player.completeMandatory();
        // Don't wait, display checkboxes while displaying the message
        List<Boolean> results;
        results = player.showItems(title, choices, values, false);
        MappedState mappedState = (MappedState) state;
        for (int i = 0; i < indices.size(); i++) {
            Integer n = indices.get(i);
            if (results.get(i) == true) {
                // Handle mapped values
                if (mappedState.hasMapping(n)) {
                    Items<Toys> items = mappedState.getMappedItems(n);
                    if (items.size() == 1) {
                        // Just a single item - just set
                        state.set(n);
                    } else if (items.available().size() > 0) {
                        // Nothing to do, already applied
                        // Cache result
                        // mappedState.setOverride(n);
                    } else {
                        // Render message for selecting the mapped items
                        Action action2 = script.actions.get(n);
                        if (action2 == null) {
                            throw new ScriptExecutionError(
                                    "Missing mapping action for " + n, script);
                        }
                        LinkedHashMap<Statement, Visual> visuals2 = action2.visuals;
                        if (visuals2 != null) {
                            for (Visual visual : visuals2.values()) {
                                player.render(visual);
                            }
                        }
                        boolean anySet = checkDetailedItems(player, title,
                                items);
                        if (anySet) {
                            // Update, cache result
                            mappedState.setOverride(n);
                            // execute the state-related part of the action
                            action2.execute(state);
                        } else {
                            state.unset(n);
                        }
                    }
                } else {
                    state.set(n);
                }
            } else {
                state.unset(n);
            }
        }
        return rangeProvider.getRange(script, action, null, player);
    }

    private static boolean checkDetailedItems(Player player, String title,
            Items<Toys> items) {
        // Ask which items of the category have been set
        List<Boolean> itemValues = new ArrayList<Boolean>();
        List<String> itemChoices = new ArrayList<String>();
        for (Item<Toys> item : items) {
            itemValues.add(item.isAvailable());
            itemChoices.add(item.displayName);
        }
        // The check box title is reused
        List<Boolean> itemResults = player.showItems(title, itemChoices,
                itemValues, false);
        // Apply changes to category items
        boolean anySet = false;
        for (int j = 0; j < itemResults.size(); j++) {
            Boolean isAvailable = itemResults.get(j);
            anySet |= isAvailable;
            items.get(j).setAvailable(isAvailable);
        }
        return anySet;
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
