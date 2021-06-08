package pcm.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pcm.model.Action;

/**
 * @author Citizen-Cane
 *
 */
public class BreakPoints {
    static class BreakPointMap extends HashMap<Integer, BreakPoint> {
        private static final long serialVersionUID = 1L;
    }

    private Map<String, BreakPointMap> breakPoints = new HashMap<>();

    public void add(String script, Trigger... triggers) {
        for (Trigger trigger : triggers) {
            add(script, trigger.actions(), trigger);
        }
    }

    public void add(String script, List<Trigger> triggers) {
        for (Trigger trigger : triggers) {
            add(script, trigger.actions(), trigger);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BreakPoint> T add(String script, Action action) {
        return add(script, Collections.singleton(action), (T) BreakPoint.STOP);
    }

    public <T extends Trigger> T add(String script, T trigger) {
        return add(script, trigger.actions(), trigger);
    }

    public <T extends BreakPoint> T add(String script, Action action, T breakPoint) {
        return add(script, Collections.singleton(action), breakPoint);
    }

    public <T extends BreakPoint> T add(String script, Set<Action> actions, T breakPoint) {
        BreakPointMap bpm = breakPoints.computeIfAbsent(script, key -> new BreakPointMap());
        actions.stream().forEach(a -> bpm.put(a.number, breakPoint));
        return breakPoint;
    }

    public BreakPoint getBreakPoint(String script, Action action) {
        BreakPointMap bpm = breakPoints.get(script);
        if (bpm == null) {
            return BreakPoint.NONE;
        }
        var breakPoint = bpm.get(action.number);
        if (breakPoint == null) {
            return BreakPoint.NONE;
        }
        return breakPoint;
    }

}
