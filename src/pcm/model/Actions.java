package pcm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<Action> getAll(ActionRange range) {
        // TODO Not exactly performant, but improvements are tricky
        List<Action> actionRange = new ArrayList<Action>();
        for (int i = range.start; i <= range.end; i++) {
            Integer index = new Integer(i);
            if (actions.containsKey(index)) {
                actionRange.add(actions.get(index));
            }
        }
        return actionRange;

    }

    public void validate(Script script, Action action, ActionRange range,
            List<ValidationError> validationErrors) {
        List<Action> actions = getAll(range);
        if (actions.isEmpty()) {
            validationErrors.add(new ValidationError(action, "Range "
                    + range.start + "-" + range.end + " is empty", script));
        } else if (actions.get(0).number != range.start) {
            validationErrors.add(new ValidationError(action, "Range "
                    + range.start + "-" + range.end + " must start at "
                    + range.start, script));
        }
    }

    public Collection<Action> values() {
        return actions.values();
    }
}
