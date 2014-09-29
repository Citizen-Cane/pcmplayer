package pcm.state.Interactions;

import java.util.ArrayList;
import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Stop implements Interaction, NeedsRangeProvider {

	private final ActionRange stop;
	private Interaction rangeProvider = null;

	public Stop(ActionRange stop) {
		this.stop = stop;
	}

	@Override
	public ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError {
		String stopText = stopText(script, action);
		TeaseLib.log(getClass().getSimpleName() + " " + toString());
		List<String> choice = new ArrayList<>(1);
		choice.add(stopText);
		int result = teaseScript.choose(choice, visuals);
		if (result != TeaseScript.Timeout) {
			TeaseLib.log("-> Stop");
			teaseScript.teaseLib.host.stopSounds();
			return stop;
		} else {
			return rangeProvider.getRange(script, action, null, teaseScript);
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
		return  stop.toString();
	}

	@Override
	public void validate(Script script, Action action,
			List<ValidationError> validationErrors) throws ParseError {
		script.actions.validate(action, stop, validationErrors);
		if (rangeProvider != null) {
			rangeProvider.validate(script, action, validationErrors);
		}
	}
}
