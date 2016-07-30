package pcm.state;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;

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
 * TODO add full test coverage as this central class responsible for the flow of
 * execution is completely untested
 * 
 * * @author someone
 *
 */
public class State {
    private static final Logger logger = LoggerFactory.getLogger(State.class);

    public final Player player;

    private Script script = null;

    /**
     * Actions are set separately, they're reset each time a script is loaded.
     */
    private Set<Integer> actions = new HashSet<Integer>();

    /**
     * Data is never cleared when loading a script, but they're all cleared when
     * the startup script is run.
     * 
     * Instead, at script start the save range is restored, followed by
     * additional .resetrange commands.
     */
    private Map<Integer, Long> data = new HashMap<Integer, Long>();

    /**
     * Action may also contain a time stamp.
     */
    private Map<Integer, Date> times = new HashMap<Integer, Date>();

    private int step = 0;
    private Map<Integer, Integer> action2StepMap = new HashMap<Integer, Integer>();

    public static final Long SET = new Long(1);
    public static final Long UNSET = new Long(0);

    private static final String TIMEKEYS = "TimeKeys";

    public State(Player player) {
        this.player = player;
    }

    // TODO Mapping between Mine and SS properties,
    // probably a mapping between integers and human-readable names,
    // and if I ever get another host, names could be the same,
    // or they're injected by the groovy script

    public void restore(Script script) {
        this.script = script;
        step = 0;
        actions.clear();
        action2StepMap.clear();
        // Restore restores:
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
                for (int i = s; i <= e; i++) {
                    String value = read(Integer.toString(i));
                    // Avoid storing UNSET values from data
                    if (value == null) {
                        if (data.containsKey(i)) {
                            data.remove(i);
                        }
                    } else {
                        Long v = new Long(value);
                        if (v.equals(SET)) {
                            data.put(i, SET);
                        } else if (v.equals(UNSET)) {
                            data.remove(i);
                        } else {
                            data.put(i, v);
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
        // Data values
        for (int i : range) {
            Long value = get(i);
            write(Integer.toString(i), value.equals(UNSET) ? null : value);
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
        return player.getString(script.name + "." + name);
    }

    private void write(String name, Object value) {
        player.set(script.name + "." + name,
                value != null ? value.toString() : null);
    }

    public Long get(Integer n) {
        if (data.containsKey(n)) {
            return data.get(n);
        } else if (actions.contains(n)) {
            return SET;
        } else {
            return UNSET;
        }
    }

    public void set(Integer n) {
        data.put(n, SET);
        actions.remove(n);
        times.remove(n);
    }

    public void set(Collection<Integer> set) {
        for (Integer n : set) {
            set(n);
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
        data.remove(n);
        times.put(n, date);
    }

    public void unset(Collection<Integer> unset) {
        for (Integer n : unset) {
            unset(n);
        }
    }

    public void unset(Integer n) {
        actions.remove(n);
        data.remove(n);
        times.remove(n);
    }

    public void resetRange(int start, int end) {
        for (int i = start; i <= end; i++) {
            Integer n = new Integer(i);
            unset(n);
        }
    }

    public void repeatSet(Integer n, int m) {
        Integer v = -m;
        data.put(n, v.equals(SET) ? SET : v);
        actions.remove(n);
        times.remove(n);
    }

    public void repeatAdd(Integer n, int m) {
        Long v = data.containsKey(n) ? data.get(n) : UNSET;
        Long w = v.equals(SET) ? -m : v - m;
        logger.info("Increasing " + n + " from " + v + " to " + w);
        data.put(n, w);
        actions.remove(n);
        times.remove(n);
    }

    public void repeatDel(Integer n, int m) {
        long v = data.containsKey(n) ? data.get(n) : UNSET;
        Long w = v + m < SET ? new Long(v) + m : SET;
        logger.info("Decreasing " + n + " from " + v + " to " + w);
        data.put(n, w);
        actions.remove(n);
        times.remove(n);
    }

    /**
     * Milliseconds since midnight 1.1.1970 UTC
     * 
     * @return
     */
    public long getTimeMillis() {
        return player.teaseLib.getTime(TimeUnit.MILLISECONDS);
    }

    public void set(Action action) throws ScriptExecutionException {
        Integer n = action.number;
        final Long value = get(n);
        if (value.equals(SET)) {
            throw new ScriptExecutionException("Action already set");
        } else if (value.equals(UNSET)) {
            // advance from UNSET to SET
            // Action sets are not saved, and unlike data sets cleared on
            // restore
            actions.add(n);
            data.remove(n);
            times.remove(n);
            rememberActionStep(n);
        } else {
            // count up from n to UNSET == 0
            repeatDel(n, 1);
            rememberActionStep(n);
        }
    }

    private void rememberActionStep(Integer n) {
        if (!action2StepMap.containsKey(n)) {
            action2StepMap.put(n, new Integer(step));
        }
        step++;
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
        return player.random(min, max);
    }
}
