package pcm.controller;

import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;

public class AllActionsSetException extends ScriptExecutionError {

	private static final long serialVersionUID = 1L;

	public AllActionsSetException(Action action, Script script)
	{
		this(action);
		this.script = script;
	}

	private AllActionsSetException(Action action)
	{
		super(action != null
				? "All actions set: " + action.number
						: "All actions set in script section");
		// TODO Debug info
	}
}
