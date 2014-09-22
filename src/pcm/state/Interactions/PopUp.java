package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.MenuItem;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class PopUp implements Interaction {
	private final int start;
	private final int end;

	public PopUp(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError {
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
		TeaseLib.log(getClass().getSimpleName() + " " + choices.toString());
		visuals.run();
		int index = teaseScript.choose(choices);
		return items.get(index).range;
	}

	@Override
	public void validate(Script script, Action action,
			List<ValidationError> validationErrors) {
	}
}
