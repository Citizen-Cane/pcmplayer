package pcm.controller;

import java.util.List;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;

public class AllActionsSetException extends ScriptExecutionException {
    private static final long serialVersionUID = 1L;

    public final List<Action> actions;

    public AllActionsSetException(Script script, Action action, ActionRange range, List<Action> actions) {
        super(script, action, (actions.size() > 1 ? "All actions set: " : "Empty range ") + range);
        this.actions = actions;
    }
}
