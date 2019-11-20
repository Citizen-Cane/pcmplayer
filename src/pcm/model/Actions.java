package pcm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pcm.state.persistence.ScriptState;

/**
 * Enumerate actions. - Find action by index - Enum action range and evaluate conditions in order to choose next action
 * 
 * @author someone
 *
 */
public class Actions {
    private Map<Integer, Action> map = new LinkedHashMap<>();

    public Action put(Integer n, Action action) {
        return map.put(n, action);
    }

    public Action get(int n) {
        if (map.containsKey(n)) {
            return map.get(Integer.valueOf(n));
        } else {
            return null;
        }
    }

    public List<Action> getAll() {
        return new ArrayList<>(map.values());
    }

    /**
     * Get all actions in the given range.
     * 
     * @param range
     * @return
     */
    public List<Action> getAll(ActionRange range) {
        List<Action> actionRange = new ArrayList<>();
        for (int i = range.start; i <= range.end; i++) {
            Integer index = Integer.valueOf(i);
            if (map.containsKey(index)) {
                actionRange.add(map.get(index));
            }
        }
        return actionRange;
    }

    /**
     * Get all actions in the given range that are unset, e.g haven't been executed or set otherwise.
     * 
     * @param range
     * @param state
     * @return List of actions
     */
    public List<Action> getUnset(ActionRange range, ScriptState state) {
        List<Action> actionRange = new ArrayList<>();
        for (int i = range.start; i <= range.end; i++) {
            Integer index = Integer.valueOf(i);
            if (map.containsKey(index)) {
                Action action = map.get(index);
                if (!state.get(Integer.valueOf(action.number)).equals(ScriptState.SET)) {
                    actionRange.add(map.get(index));
                }
            }
        }
        return actionRange;
    }

    public void validate(Script script, Action action, ActionRange range, List<ValidationIssue> validationErrors) {
        List<Action> allInRange = getAll(range);
        if (allInRange.isEmpty()) {
            validationErrors.add(new ValidationIssue(script, action, "Range " + range + " is empty"));
        } else if (allInRange.get(0).number != range.start) {
            validationErrors
                    .add(new ValidationIssue(script, action, "Range " + range + " must start at " + range.start));
        }
    }

    public Collection<Action> values() {
        return map.values();
    }

    public int size() {
        return map.size();
    }
}
