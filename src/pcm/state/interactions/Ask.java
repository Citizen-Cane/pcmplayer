package pcm.state.interactions;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

    private final ActionRange range;
    private final List<AskItem> askItems = new ArrayList<>();

    private ScriptState state = null;
    private Interaction rangeProvider = null;

    public Ask(ActionRange range, Script script) {
        this.range = range;
        for (Entry<Integer, AskItem> entry : script.askItems.entrySet()) {
            if (range.contains(entry.getKey())) {
                askItems.add(entry.getValue());
            }
        }
    }

    @Override
    public void execute(ScriptState state) {
        this.state = state;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        List<String> choices = new ArrayList<>();
        List<Boolean> values = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        String title = null;
        for (AskItem askItem : askItems) {
            if (askItem.action == 0) {
                title = askItem.title;
            } else {
                int condition = askItem.condition;
                if (condition == AskItem.ALWAYS || state.get(condition).equals(ScriptState.SET)) {
                    Boolean value = state.get(askItem.action).equals(ScriptState.SET) ? TRUE : FALSE;
                    values.add(value);
                    choices.add(askItem.title);
                    indices.add(askItem.action);
                }
            }
        }

        logger.info("{} {}", getClass().getSimpleName(), choices);
        visuals.run();
        player.completeMandatory();
        // Don't wait, display checkboxes while displaying the message
        List<Boolean> results;
        results = player.showItems(title, choices, values, false);
        MappedScriptState mappedState = (MappedScriptState) state;
        for (int i = 0; i < indices.size(); i++) {
            Integer n = indices.get(i);
            boolean setItemsAvailable = results.get(i);
            if (setItemsAvailable) {
                // Handle mapped values
                if (mappedState.hasScriptValueMapping(n)) {
                    Items items = mappedState.getMappedItems(n);
                    List<Item> itemList = items.stream().collect(Collectors.toList());
                    if (itemList.isEmpty()) {
                        throw new ScriptExecutionException("Undefined items in mapping " + n, script);
                    } else {
                        if (itemList.size() == 1) {
                            // single item - just set
                            state.set(n);
                        } else if (items.anyAvailable()) {
                            // Update availability state
                            mappedState.setIgnoreMapping(n);
                        } else {
                            // Execute action for selecting the mapped items
                            Action detailAction = script.actions.get(n);
                            if (detailAction == null) {
                                throw new ScriptExecutionException("Missing mapping action for " + n, script);
                            }
                            Map<Statement, Visual> detailVisuals = detailAction.visuals;
                            if (detailVisuals != null) {
                                for (Visual visual : detailVisuals.values()) {
                                    player.render(visual);
                                }
                            }
                            boolean anySet = checkDetailedItems(player, title, items);
                            if (anySet) {
                                // Update, cache result
                                mappedState.setIgnoreMapping(n);
                                // execute the state-related part of the action
                                detailAction.execute(state);
                            } else {
                                state.unset(n);
                            }
                        }
                    }
                } else {
                    state.set(n);
                }
            } else {
                state.unset(n);
            }
        }
        return rangeProvider.getRange(player, script, action, NoVisuals);
    }

    private static boolean checkDetailedItems(Player player, String title, Items items) {
        // Ask which items of the category have been set
        List<Item> itemList = new ArrayList<>();
        List<String> itemChoices = new ArrayList<>();
        List<Boolean> itemValues = new ArrayList<>();
        for (Item item : items) {
            itemList.add(item);
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
            itemList.get(j).setAvailable(isAvailable);
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

    @Override
    public List<ActionRange> coverage() {
        List<ActionRange> coverage = new ArrayList<>();
        coverage.add(range);
        coverage.addAll(rangeProvider.coverage());
        askItems.stream().map(askItem -> ActionRange.of(askItem.action)).forEach(coverage::add);
        return coverage;
    }

}
