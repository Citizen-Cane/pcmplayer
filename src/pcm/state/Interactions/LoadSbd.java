package pcm.state.Interactions;

import java.io.IOException;
import java.util.List;

import pcm.model.Action;
import pcm.model.ActionLoadSbd;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Interaction;
import teaselib.TeaseScript;

public class LoadSbd implements Interaction {
	private final String scriptName;
	private final int start;
	private final int end;

	public LoadSbd(String script, int start, int end) {
		this.scriptName = script;
		this.start = start;
		this.end = end;
	}

	@Override
	public ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError {
		visuals.run();
		Script loadSbd;
		try {
			loadSbd = script.load(scriptName);
		} catch (ParseError e) {
			throw new ScriptExecutionError(action, "Failed to parse script "
					+ scriptName, e, script);
		} catch (ValidationError e) {
			throw new ScriptExecutionError(action, "Failed to validate script "
					+ scriptName, e, script);
		} catch (IOException e) {
			throw new ScriptExecutionError(action, "Failed to load script "
					+ scriptName, e, script);
		}
		return new ActionLoadSbd(loadSbd, start, end);
	}

	@Override
	public void validate(Script script, Action action,
			List<ValidationError> validationErrors) throws ParseError {
		try {
			Script loadSbd = script.load(scriptName);
			loadSbd.actions.validate(action, new ActionRange(start, end), validationErrors);
		} catch (ParseError e) {
			throw e;
		} catch (ValidationError e) {
			validationErrors.add(e);
		} catch (IOException e) {
			validationErrors.add(new ValidationError(action, e));
		}
	}
}
