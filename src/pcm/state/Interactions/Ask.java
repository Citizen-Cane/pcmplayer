package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.AskItem;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Command;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.Visual;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.ScriptState;
import teaselib.util.Item;
import teaselib.util.Items;

public class Ask implements Command, Interaction, NeedsRangeProvider {
    private static final Logger logger = LoggerFactory.getLogger(Ask.class);

    private final int start;
    private final int end;

    private ScriptState state = null;

    private Interaction rangeProvider = null;

    public Ask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void execute(ScriptState state) {
        this.state = state;
    }

    @Override
    public ActionRange getRange(Script script, Action action, Runnable visuals, Player player)
            throws ScriptExecutionException {
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
                            || state.get(condition).equals(ScriptState.SET)) {
                        Boolean value = state.get(askItem.action) == ScriptState.SET ? Boolean.TRUE
                                : Boolean.FALSE;
                        values.add(value);
                        choices.add(askItem.title);
                        indices.add(new Integer(askItem.action));
                    }
                }
            }
        }
        logger.info(getClass().getSimpleName() + " " + choices.toString());
        visuals.run();
        player.completeMandatory();
        // Don't wait, display checkboxes while displaying the message
        List<Boolean> results;
        results = player.showItems(title, choices, values, false);
        MappedScriptState mappedState = (MappedScriptState) state;
        for (int i = 0; i < indices.size(); i++) {
            Integer n = indices.get(i);
            if (results.get(i) == true) {
                // Handle mapped values
                if (mappedState.hasScriptValueMapping(n)) {
                    Items items = mappedState.getMappedItems(n);
                    if (items.isEmpty()) {
                        throw new ScriptExecutionException("Undefined items in mapping " + n,
                                script);
                    } else if (items.size() == 1) {
                        // Just a single item - just set
                        state.set(n);
                    } else if (items.available().size() > 0) {
                        // Update, cache result
                        mappedState.setOverride(n);
                    } else {
                        // Execute action for selecting the mapped items
                        Action detailAction = script.actions.get(n);
                        if (detailAction == null) {
                            throw new ScriptExecutionException("Missing mapping action for " + n,
                                    script);
                        }
                        LinkedHashMap<Statement, Visual> detailVisuals = detailAction.visuals;
                        if (detailVisuals != null) {
                            for (Visual visual : detailVisuals.values()) {
                                player.render(visual);
                            }
                        }
                        boolean anySet = checkDetailedItems(player, title, items);
                        if (anySet) {
                            // Update, cache result
                            mappedState.setOverride(n);
                            // execute the state-related part of the action
                            detailAction.execute(state);
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
        return rangeProvider.getRange(script, action, NoVisuals, player);
    }

    private static boolean checkDetailedItems(Player player, String title, Items items) {
        // Ask which items of the category have been set
        List<Boolean> itemValues = new ArrayList<Boolean>();
        List<String> itemChoices = new ArrayList<String>();
        for (Item item : items) {
            itemValues.add(item.isAvailable());
            itemChoices.add(item.displayName());
        }
        // The check box title is reused
        List<Boolean> itemResults = player.showItems(title, itemChoices, itemValues, false);
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
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        if (rangeProvider != null) {
            rangeProvider.validate(script, action, validationErrors);
        }
    }
}
