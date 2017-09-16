package pcm.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Citizen-Cane
 *
 */
public class BreakPoints {
    static class BreakPointMap extends HashMap<Integer, BreakPoint> {
        private static final long serialVersionUID = 1L;
    }

    private Map<String, BreakPointMap> breakPoints = new HashMap<String, BreakPoints.BreakPointMap>();

    public void add(String script, int action) {
        add(script, action, BreakPoint.STOP);
    }

    public void add(String script, int action, BreakPoint condition) {
        BreakPointMap bpm = breakPoints.get(script);
        if (bpm == null) {
            breakPoints.put(script, bpm = new BreakPointMap());
        }
        bpm.put(action, condition);
    }

    public BreakPoint getBreakPoint(String script, int action) {
        BreakPointMap bpm = breakPoints.get(script);
        if (bpm == null) {
            return BreakPoint.NONE;
        }
        BreakPoint condition = bpm.get(action);
        if (condition == null) {
            return BreakPoint.NONE;
        }
        return condition;
    }
}
