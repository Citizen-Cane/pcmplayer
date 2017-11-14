package pcm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            add(script, trigger.action, trigger);
        }
    }

    public void add(String script, List<Trigger> triggers) {
        for (Trigger trigger : triggers) {
            add(script, trigger.action, trigger);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BreakPoint> T add(String script, int action) {
        return add(script, action, (T) BreakPoint.STOP);
    }

    public <T extends Trigger> T add(String script, T trigger) {
        return add(script, trigger.action, trigger);
    }

    public <T extends BreakPoint> T add(String script, int action, T breakPoint) {
        BreakPointMap bpm = breakPoints.get(script);
        if (bpm == null) {
            bpm = new BreakPointMap();
            breakPoints.put(script, bpm);
        }
        bpm.put(action, breakPoint);

        return breakPoint;
    }

    public BreakPoint getBreakPoint(String script, int action) {
        BreakPointMap bpm = breakPoints.get(script);
        if (bpm == null) {
            return BreakPoint.NONE;
        }
        BreakPoint breakPoint = bpm.get(action);
        if (breakPoint == null) {
            return BreakPoint.NONE;
        }
        return breakPoint;
    }

}
