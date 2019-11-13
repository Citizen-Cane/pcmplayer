package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final int start;
    private final int end;
    private final List<MenuItem> menuItems = new ArrayList<>();

    public PopUp(int start, int end, Script script) {
        this.start = start;
        this.end = end;
        Map<Integer, MenuItem> all = script.menuItems;
        for (int i = start; i <= end; i++) {
            Integer index = Integer.valueOf(i);
            if (all.containsKey(index)) {
                MenuItem menuItem = all.get(index);
                this.menuItems.add(menuItem);
            }
        }
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        List<String> choices = menuItems.stream().map(menuItem -> menuItem.message).collect(Collectors.toList());
        logger.info("{} {}", getClass().getSimpleName(), choices);
        visuals.run();
        player.completeMandatory();
        String result = player.reply(choices);
        return menuItems.get(choices.indexOf(result)).range;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
        for (MenuItem menuItem : menuItems) {
            if (script.actions.getAll(menuItem.range).isEmpty()) {
                validationErrors.add(new ValidationIssue(action, "Empty range " + menuItem.range, script));
            }
        }
    }

    @Override
    public List<ActionRange> coverage() {
        List<ActionRange> coverage = new ArrayList<>(menuItems.size() + 1);
        coverage.add(new ActionRange(start, end));
        menuItems.stream().map(menuItem -> menuItem.range).forEach(coverage::add);
        return coverage;
    }

}
