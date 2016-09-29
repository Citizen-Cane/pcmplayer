/**
 * 
 */
package pcm.state;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import pcm.controller.Player;
import pcm.model.Script;
import teaselib.Toys;
import teaselib.util.Item;
import teaselib.util.Items;

/**
 * Adds mapping to host persistence. One use case is the mapping from host to
 * pcm toys.
 * 
 * The mapping allows to map multiple host values to a single pcm values, in
 * order to be able to map toy categories. Gags are a good example for this,
 * since Mine requires just a gag, but doesn't care about the specific kind.
 * 
 * @author someone
 *
 */
/**
 * @author someone
 *
 */
public class MappedState extends State {
    public static final String Global = "";

    private static class ScriptMapping {
        final Map<Integer, Items<Toys>> toyMapping = new HashMap<Integer, Items<Toys>>();
        final Map<Integer, teaselib.State> stateMapping = new HashMap<Integer, teaselib.State>();
        final Map<Integer, teaselib.State> stateTimeMapping = new HashMap<Integer, teaselib.State>();

        public void putAll(ScriptMapping globalMapping) {
            toyMapping.putAll(globalMapping.toyMapping);
            stateMapping.putAll(globalMapping.stateMapping);
            stateTimeMapping.putAll(globalMapping.stateTimeMapping);
        }
    }

    Map<String, ScriptMapping> scriptMappings = new HashMap<String, ScriptMapping>();
    ScriptMapping scriptMapping = null;

    public MappedState(Player player) {
        super(player);
    }

    @Override
    public void restore(Script script) {
        scriptMapping = getScriptMapping(script.name);
        ScriptMapping globalMapping = getScriptMapping(Global);
        scriptMapping.putAll(globalMapping);
        super.restore(script);
    }

    private ScriptMapping getScriptMapping(String name) {
        ScriptMapping existing = scriptMappings.get(name);
        if (existing == null) {
            ScriptMapping newMapping = new ScriptMapping();
            scriptMappings.put(name, newMapping);
            return newMapping;
        } else {
            return existing;
        }
    }

    public void addToyMapping(String scriptName, Integer action,
            Item<Toys> item) {
        Items<Toys> items = new Items<Toys>();
        items.add(item);
        getScriptMapping(scriptName).toyMapping.put(action, items);
    }

    public void addToyMapping(String scriptName, Integer action,
            Items<Toys> items) {
        getScriptMapping(scriptName).toyMapping.put(action, items);
    }

    public void addStateMapping(String scriptName, Integer action,
            teaselib.State state) {
        getScriptMapping(scriptName).stateMapping.put(action, state);
    }

    public void addStateTimeMapping(String scriptName, Integer action,
            teaselib.State state) {
        getScriptMapping(scriptName).stateTimeMapping.put(action, state);
    }

    @Override
    public Long get(Integer n) {
        if (hasToyMapping(n)) {
            if (scriptMapping.toyMapping.get(n).available().size() > 0) {
                super.set(n);
                return SET;
            } else {
                super.unset(n);
                return UNSET;
            }
        } else if (hasStateMapping(n)) {
            if (scriptMapping.stateMapping.get(n).applied()) {
                super.set(n);
                return SET;
            } else {
                super.unset(n);
                return UNSET;
            }
        } else {
            return super.get(n);
        }
    }

    public boolean hasToyMapping(Integer n) {
        return scriptMapping.toyMapping.containsKey(n);
    }

    public Items<Toys> getMappedToys(Integer n) {
        return scriptMapping.toyMapping.get(n);
    }

    private boolean hasStateMapping(Integer n) {
        return scriptMapping.stateMapping.containsKey(n);
    }

    private boolean hasStateTimeMapping(Integer n) {
        return scriptMapping.stateTimeMapping.containsKey(n);
    }

    /*
     * @see pcm.state.State#set(java.lang.Integer)
     */
    @Override
    public void set(Integer n) {
        if (hasToyMapping(n)) {
            Items<Toys> items = scriptMapping.toyMapping.get(n);
            if (items.size() == 1) {
                // 1:1 mapping
                items.get(0).setAvailable(true);
            } else {
                throw new IllegalStateException(n + "(" + items.toString() + ")"
                        + ": Multiple-mapped values can only be unset");
            }
        } else if (hasStateMapping(n)) {
            scriptMapping.stateMapping.get(n).apply();
        }
        super.set(n);
    }

    public void setOverride(Integer n) {
        if (hasToyMapping(n)) {
            super.set(n);
        } else {
            throw new IllegalStateException(
                    "setOverride can only be called for toy mappings");
        }
    }

    @Override
    public void unset(Integer n) {
        if (hasToyMapping(n)) {
            Items<Toys> items = scriptMapping.toyMapping.get(n);
            for (Item<Toys> item : items) {
                item.setAvailable(false);
            }
        } else if (hasStateMapping(n)) {
            scriptMapping.stateMapping.get(n).remove();
        }
        super.unset(n);
    }

    @Override
    public Date getTime(Integer n) {
        if (hasStateTimeMapping(n)) {
            teaselib.State state = scriptMapping.stateTimeMapping.get(n);
            long time = (state.getDuration().startSeconds + state.expected())
                    * 1000;
            Date date = new Date(time);
            return date;
        } else {
            return super.getTime(n);
        }
    }

    @Override
    public void setTime(Integer n, Date date) {
        if (hasStateTimeMapping(n)) {
            teaselib.State state = scriptMapping.stateTimeMapping.get(n);
            long now = System.currentTimeMillis();
            long duration = date.getTime() - now;
            state.apply(duration, TimeUnit.MILLISECONDS);
        }
        super.setTime(n, date);
    }

    @Override
    public void overwrite(Integer n, Long value) {
        scriptMapping.toyMapping.remove(n);
        scriptMapping.stateMapping.remove(n);
        scriptMapping.stateTimeMapping.remove(n);
        super.overwrite(n, value);
    }

    public void overwrite(Toys toy, boolean available) {
        Entry<Integer, Items<Toys>> entry = getEntry(toy);
        if (entry != null) {
            overwrite(entry.getKey(), available ? SET : UNSET);
        }
    }

    private Entry<Integer, Items<Toys>> getEntry(Toys toy) {
        for (Entry<Integer, Items<Toys>> entry : scriptMapping.toyMapping
                .entrySet()) {
            Items<Toys> toys = entry.getValue();
            for (Item<Toys> item : toys) {
                if (item.item == toy) {
                    return entry;
                }
            }
        }
        return null;
    }
}
