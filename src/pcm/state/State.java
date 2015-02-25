package pcm.state;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionError;
import teaselib.Host;
import teaselib.Persistence;

/**
 * Well, the original PCMistress likely used a single (long?) array for storing
 * values of any kind - set/unset: just a bool - RepeatAdd, RepeatDelete: a
 * number, when 0 set returns true - setTime appears to have stored hours and
 * minutes, but has missed to store seconds -> corrected in this player by
 * enforcing the use of the add-on command '.timefrom
 * 
 * The storing scheme looks weird at first, but implements SET as 1, UNSET as 0.
 * As a result, repeatAdd/Delete counts up with negative numbers, and the repeat
 * count evaluates to (-n)-1.
 * 
 * set() and unset() actually set the value to either 1 or 0, whereas
 * resetRange() removes the value from the storage.
 * 
 * * @author someone
 *
 */
public class State {

    private Script script = null;
    private final Host renderer;
    private final Persistence persistence;

    private Map<Integer, Integer> data = new HashMap<Integer, Integer>();
    private Map<Integer, Date> times = new HashMap<Integer, Date>();

    private int step = 0;
    private Map<Integer, Integer> action2StepMap = new HashMap<Integer, Integer>();

    public static final Integer SET = new Integer(1);
    public static final Integer UNSET = new Integer(0);

    private static final String TIMEKEYS = "TimeKeys";

    public State(Host renderer, Persistence persistence) {
        this.renderer = renderer;
        this.persistence = persistence;
    }

    // TODO Mapping between Mine and SS properties,
    // probably a mapping between integers and human-readable names,
    // and if I ever get another host, names could be the same,
    // or they're injected by the groovy script

    public void restore(Script script) {
        this.script = script;
        step = 0;
        action2StepMap.clear();
        // Restore restores
        // - nothing if nothing has been saved before
        // - The range last saved
        String start = read("save.start");
        if (start != null) {
            int s = Integer.parseInt(start);
            if (s < 0)
                return;
            String end = read("save.end");
            if (end != null) {
                int e = Integer.parseInt(end);
                if (e < 0)
                    return;
                for (Integer i = s; i <= e; i++) {
                    String value = read(i.toString());
                    // Avoid storing UNSET values from data
                    if (value == null) {
                        if (data.containsKey(i)) {
                            data.remove(i);
                        }
                    } else {
                        Integer v = new Integer(value);
                        if (v.equals(SET)) {
                            data.put(i, SET);
                        }
                        if (v.equals(UNSET)) {
                            data.remove(i);
                        } else {
                            data.put(i, SET);
                        }
                    }
                }
                String timeKeys = read(TIMEKEYS);
                if (timeKeys != null) {
                    for (StringTokenizer keys = new StringTokenizer(timeKeys,
                            " "); keys.hasMoreTokens();) {
                        String key = keys.nextToken();
                        long value = Long.parseLong(read(key));
                        times.put(new Integer(key), new Date(value * 1000));
                    }
                }
            }
        }
    }

    public void save(ActionRange range) {
        write("save.start", range.start);
        write("save.end", range.end);
        for (Integer i : range) {
            Integer value = get(i);
            write(i.toString(), value.equals(UNSET) ? null : value);
        }
        // time values
        StringBuilder keys = null;
        for (Integer n : times.keySet()) {
            if (range.contains(n)) {
                String number = n.toString();
                if (keys == null) {
                    keys = new StringBuilder(number);
                } else {
                    keys.append(" ");
                    keys.append(number);
                }
                // Save seconds
                write(number,
                        new Long(times.get(n).getTime() / 1000).toString());
            }
        }
        if (keys != null) {
            write(TIMEKEYS, keys.toString());
        } else {
            write(TIMEKEYS, "");
        }
    }

    private String read(String name) {
        return persistence.get(script.name + "." + name);
    }

    private void write(String name, Object value) {
        persistence.set(script.name + "." + name,
                value != null ? value.toString() : null);
    }

    public Integer get(int n) {
        Integer n_ = n;
        if (data.containsKey(n_)) {
            return data.get(n_);
        } else {
            return UNSET;
        }
    }

    public Integer get(Integer n) {
        if (data.containsKey(n)) {
            return data.get(n);
        } else {
            return UNSET;
        }
    }

    public void set(int n) {
        data.put(new Integer(n), SET);
        times.remove(n);
    }

    public void set(Collection<Integer> set) {
        for (Integer n : set) {
            data.put(n, SET);
            times.remove(n);
        }
    }

    public Date getTime(Integer n) {
        if (times.containsKey(n)) {
            return times.get(n);
        } else {
            return null;
        }
    }

    public void setTime(Integer n, Date date) {
        times.put(n, date);
    }

    public void unset(Collection<Integer> unset) {
        for (Integer n : unset) {
            unset(n);
        }
    }

    public void unset(Integer n) {
        data.put(n, UNSET);
        times.remove(n);
    }

    public void resetRange(int start, int end) {
        for (int i = start; i <= end; i++) {
            Integer n = new Integer(i);
            if (data.containsKey(n)) {
                data.remove(n);
                times.remove(n);
            }
        }
    }

    public void repeatSet(Integer n, int m) {
        Integer v = -m;
        data.put(n, v.equals(SET) ? SET : v);
    }

    public void repeatAdd(Integer n, int m) {
        Integer v = data.containsKey(n) ? data.get(n) - m : -m;
        data.put(n, v.equals(SET) ? SET : v);
    }

    public void repeatDel(Integer n, int m) {
        int v = data.containsKey(n) ? data.get(n) + m : 0;
        data.put(n, v < SET ? new Integer(v) : SET);
    }

    public long getTime() {
        return renderer.getTime();
    }

    public void set(Action action) throws ScriptExecutionError {
        Integer n = action.number;
        if (get(n).equals(SET)) {
            throw new ScriptExecutionError("Action already set");
        } else {
            set(n);
            if (!action2StepMap.containsKey(n)) {
                action2StepMap.put(n, new Integer(step));
            }
            step++;
        }
    }

    public int getStep() {
        return step;
    }

    public int getStep(Action action) {
        return getStep(action.number);
    }

    public int getStep(Integer n) {
        if (!action2StepMap.containsKey(n)) {
            return 0;
        } else {
            return action2StepMap.get(n);
        }
    }

    public int getRandom(int min, int max) {
        return renderer.getRandom(min, max);
    }
}
