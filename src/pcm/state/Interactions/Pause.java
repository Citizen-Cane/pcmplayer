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
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Pause implements Interaction, Interaction.NeedsRangeProvider {
	private Interaction rangeProvider = null;

	@Override
	public ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError {
		String resume = resumeText(script, action);
		TeaseLib.log("Pause: " + resume);
		List<String> choices = new ArrayList<>(1);
		choices.add(resume);
		visuals.run();
		teaseScript.choose(choices);
		return rangeProvider.getRange(script, action, null, teaseScript);
	}

	private String resumeText(Script script, Action action) {
		return action.resumeText != null ? action.resumeText
				: script.resumeText;
	}

	@Override
	public void setRangeProvider(Interaction rangeProvider) {
		this.rangeProvider = rangeProvider;
	}

	@Override
	public void validate(Script script, Action action,
			List<ValidationError> validationErrors) throws ParseError {
		if (rangeProvider != null)
		{
			rangeProvider.validate(script, action, validationErrors);
		}
	}
}
