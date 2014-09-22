package pcm.state;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import teaselib.TeaseScript;

public interface Interaction {
	ActionRange getRange(Script script, Action action, Runnable visuals,
			TeaseScript teaseScript) throws ScriptExecutionError;

	void validate(Script script, Action action, List<ValidationError> validationErrors) throws ParseError;
	
	public interface NeedsRangeProvider {
		public void setRangeProvider(Interaction rangeProvider);
	}
}
