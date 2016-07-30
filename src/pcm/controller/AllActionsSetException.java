package pcm.controller;

import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;

public class AllActionsSetException extends ScriptExecutionException {

    private static final long serialVersionUID = 1L;

    public AllActionsSetException(Action action, Script script) {
        super(action != null ? "All actions set: " + action.number
                : "All actions set in script section", script);
    }
}
