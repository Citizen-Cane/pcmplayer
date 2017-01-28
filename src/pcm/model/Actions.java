package pcm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pcm.state.persistence.ScriptState;

/**
 * Enumerate actions. - Find action by index - Enum action range and evaluate
 * conditions in order to choose next action
 * 
 * @author someone
 *
 */
public class Actions {
    private Map<Integer, Action> actions = new LinkedHashMap<Integer, Action>();

    public Action put(Integer n, Action action) {
        return actions.put(n, action);
    }

    public Action get(int n) {
        if (actions.containsKey(n)) {
            return actions.get(new Integer(n));
        } else {
            return null;
        }
    }

    public List<Action> getAll() {
        return new ArrayList<Action>(actions.values());
    }

    /**
     * Get all actions in the given range.
     * 
     * @param range
     * @return
     */
    public List<Action> getAll(ActionRange range) {
        List<Action> actionRange = new ArrayList<Action>();
        for (int i = range.start; i <= range.end; i++) {
            Integer index = new Integer(i);
            if (actions.containsKey(index)) {
                actionRange.add(actions.get(index));
            }
        }
        return actionRange;
    }

    /**
     * Get all actions in the given range that are unset, e.g haven't been
     * executed or set otherwise.
     * 
     * @param range
     * @param state
     * @return List of actions
     */
    public List<Action> getUnset(ActionRange range, ScriptState state) {
        List<Action> actionRange = new ArrayList<Action>();
        for (int i = range.start; i <= range.end; i++) {
            Integer index = new Integer(i);
            if (actions.containsKey(index)) {
                Action action = actions.get(index);
                if (!state.get(new Integer(action.number)).equals(ScriptState.SET)) {
                    actionRange.add(actions.get(index));
                }
            }
        }
        return actionRange;
    }

    public void validate(Script script, Action action, ActionRange range,
            List<ValidationIssue> validationErrors) {
        List<Action> actions = getAll(range);
        if (actions.isEmpty()) {
            validationErrors.add(new ValidationIssue(action,
                    "Range " + range.start + "-" + range.end + " is empty",
                    script));
        } else if (actions.get(0).number != range.start) {
            validationErrors.add(new ValidationIssue(action,
                    "Range " + range.start + "-" + range.end + " must start at "
                            + range.start,
                    script));
        }
    }

    public Collection<Action> values() {
        return actions.values();
    }

    public int size() {
        return actions.size();
    }
}
