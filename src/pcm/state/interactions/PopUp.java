package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.MenuItem;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class PopUp implements Interaction {
    private static final Logger logger = LoggerFactory.getLogger(PopUp.class);

    private final ActionRange range;
    private final List<MenuItem> menuItems = new ArrayList<>();

    public PopUp(ActionRange range, Script script) {
        this.range = range;
        for (Entry<Integer, MenuItem> entry : script.menuItems.entrySet()) {
            if (range.contains(entry.getKey())) {
                this.menuItems.add(entry.getValue());
            }
        }
    }

    @Override
    public Action getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        List<String> choices = menuItems.stream().map(menuItem -> menuItem.message).collect(Collectors.toList());
        logger.info("{} {}", getClass().getSimpleName(), choices);
        visuals.run();
        player.completeMandatory();
        String result = player.reply(choices);
        return player.getAction(menuItems.get(choices.indexOf(result)).range);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        for (MenuItem menuItem : menuItems) {
            if (script.actions.getAll(menuItem.range).isEmpty()) {
                validationErrors.add(new ValidationIssue(script, action,
                        "menu item " + menuItem.n + " empty range " + menuItem.range));
            }
        }
    }

    @Override
    public List<ActionRange> coverage() {
        List<ActionRange> coverage = new ArrayList<>(menuItems.size() + 1);
        coverage.add(range);
        menuItems.stream().map(menuItem -> menuItem.range).forEach(coverage::add);
        return coverage;
    }

}
