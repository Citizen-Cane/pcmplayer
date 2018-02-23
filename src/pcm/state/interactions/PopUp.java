package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public PopUp(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        List<MenuItem> items = new ArrayList<>();
        List<String> choices = new ArrayList<>();
        Map<Integer, MenuItem> menuItems = script.menuItems;
        for (int i = start; i <= end; i++) {
            Integer index = new Integer(i);
            if (menuItems.containsKey(index)) {
                MenuItem menuItem = menuItems.get(index);
                items.add(menuItem);
                choices.add(menuItem.message);
            }
        }
        logger.info(getClass().getSimpleName() + " " + choices.toString());
        visuals.run();
        player.completeMandatory();
        String result = player.reply(choices);
        return items.get(choices.indexOf(result)).range;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors) {
    }
}
