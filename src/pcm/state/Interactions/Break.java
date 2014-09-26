package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import teaselib.ScriptInterruptedException;
import teaselib.TeaseLib;
import teaselib.TeaseScript;


public class Break implements Interaction,
NeedsRangeProvider {
	private ActionRange actionRange;
	private ActionRange stop;
	private Interaction rangeProvider = null;

	public Break(ActionRange actionRange, ActionRange stop) {
		this.actionRange = actionRange;
		this.stop = stop;
	}

	@Override
	public ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError {
		// First run the visuals of this actions (likely not any, but who knows)
		visuals.run();
		// TODO change TeaseScript to Player since it's just that
		Player player = (Player) teaseScript;
		String stopText = stopText(script, action);
		List<String> choice = new ArrayList<>(1);
		choice.add(stopText);
		int result = teaseScript.choose(choice, TeaseScript.NoTimeout, () -> {
			try {
				player.range = rangeProvider.getRange(script, action, null, teaseScript); 
				player.play(actionRange);
			} catch(ScriptInterruptedException e)
			{
				// Expected
			} catch (Throwable t) {
				TeaseLib.log(this, t);
			}
		});
		if (result != TeaseScript.Timeout) {
			TeaseLib.log("-> Stop");
			teaseScript.teaseLib.host.stopSounds();
			return stop;
		} else {
			return player.range;
		}
	}

	@Override
	public void setRangeProvider(Interaction rangeProvider) {
		this.rangeProvider = rangeProvider;
	}

	private String stopText(Script script, Action action) {
		return action.stopText != null ? action.stopText : script.stopText;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + actionRange.toString() + " to  stop: " + stop.toString();
	}

	@Override
	public void validate(Script script, Action action,
			List<ValidationError> validationErrors) throws ParseError {
		script.actions.validate(action, actionRange, validationErrors);
		script.actions.validate(action, stop, validationErrors);
	}
}
