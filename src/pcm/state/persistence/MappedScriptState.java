/**
 * 
 */
package pcm.state.persistence;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import pcm.controller.Player;
import pcm.model.Script;
import teaselib.Duration;
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
public class MappedScriptState extends ScriptState {
    public static final String Global = "";

    private static class ScriptMapping {
        final Map<Integer, MappedScriptValue> gameValueMapping = new HashMap<Integer, MappedScriptValue>();
        final Map<Integer, teaselib.State> stateTimeMapping = new HashMap<Integer, teaselib.State>();
        final Map<Integer, Enum<?>[]> peers = new HashMap<Integer, Enum<?>[]>();

        public void putAll(ScriptMapping globalMapping) {
            gameValueMapping.putAll(globalMapping.gameValueMapping);
            stateTimeMapping.putAll(globalMapping.stateTimeMapping);
            peers.putAll(globalMapping.peers);
        }
    }

    Map<String, ScriptMapping> scriptMappings = new HashMap<String, ScriptMapping>();
    ScriptMapping scriptMapping = null;

    public MappedScriptState(Player player) {
        super(player);
    }

    @Override
    public void setScript(Script script) {
        super.setScript(script);
        scriptMapping = getScriptMapping(script.name);
        ScriptMapping globalMapping = getScriptMapping(Global);
        scriptMapping.putAll(globalMapping);
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

    public void addScriptValueMapping(String scriptName,
            MappedScriptValue mappedGameValue) {
        ScriptMapping scriptMapping = getScriptMapping(scriptName);

        if (scriptMapping.gameValueMapping.containsValue(mappedGameValue)) {
            throw new IllegalArgumentException(
                    "Item " + mappedGameValue.toString()
                            + " is already mapped to a value.");
        }

        scriptMapping.gameValueMapping.put(mappedGameValue.getNumber(),
                mappedGameValue);
    }

    public <T extends Enum<?>> void addStateTimeMapping(String scriptName,
            Integer action, teaselib.State state, T... peers) {
        ScriptMapping scriptMapping = getScriptMapping(scriptName);

        if (scriptMapping.stateTimeMapping.containsValue(state)) {
            throw new IllegalArgumentException("State " + state.toString()
                    + " is already mapped to a timer.");
        }

        scriptMapping.stateTimeMapping.put(action, state);
        scriptMapping.peers.put(action, peers);
    }

    @Override
    public Long get(Integer n) {
        if (hasGameValueMapping(n)) {
            if (scriptMapping.gameValueMapping.get(n).isSet()) {
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

    public boolean hasGameValueMapping(Integer n) {
        if (scriptMapping == null) {
            return false;
        }
        return scriptMapping.gameValueMapping.containsKey(n);
    }

    public Items<?> getMappedItems(Integer n) {
        if (scriptMapping == null) {
            // TODO throw
            return new Items<Toys>();
        }
        return scriptMapping.gameValueMapping.get(n).items();
    }

    private boolean hasStateTimeMapping(Integer n) {
        if (scriptMapping == null) {
            return false;
        }
        return scriptMapping.stateTimeMapping.containsKey(n);
    }

    /*
     * @see pcm.state.State#set(java.lang.Integer)
     */
    @Override
    public void set(Integer n) {
        if (hasGameValueMapping(n)) {
            scriptMapping.gameValueMapping.get(n).set();
        }
        super.set(n);
    }

    public void setOverride(Integer n) {
        if (hasGameValueMapping(n)) {
            super.set(n);
        } else {
            throw new IllegalStateException(
                    "setOverride can only be called for mappings");
        }
    }

    @Override
    public void unset(Integer n) {
        if (hasGameValueMapping(n)) {
            scriptMapping.gameValueMapping.get(n).unset();
        }
        super.unset(n);
    }

    @Override
    public Date getTime(Integer n) {
        if (hasStateTimeMapping(n)) {
            teaselib.State state = scriptMapping.stateTimeMapping.get(n);
            Duration duration = state.duration();
            long timeMillis = duration.start(TimeUnit.MILLISECONDS)
                    + state.duration().limit(TimeUnit.MILLISECONDS);
            Date date = new Date(timeMillis);
            return date;
        } else {
            return super.getTime(n);
        }
    }

    @Override
    public void setTime(Integer n, long now, long offset) {
        if (hasStateTimeMapping(n)) {
            teaselib.State state = scriptMapping.stateTimeMapping.get(n);
            state.apply(scriptMapping.peers.get(n))
                    .over(player.teaseLib.new DurationImpl(now, offset,
                            TimeUnit.MILLISECONDS))
                    .remember();
        }
        super.setTime(n, now, offset);
    }

    @Override
    public void overwrite(Integer n, Long value) {
        ScriptMapping mapping = getScriptMapping(Global);
        mapping.gameValueMapping.remove(n);
        mapping.stateTimeMapping.remove(n);
        super.overwrite(n, value);
    }

    public void overwrite(Object item, boolean available) {
        Entry<Integer, MappedScriptValue> entry = getEntry(item);
        if (entry != null) {
            overwrite(entry.getKey(), available ? SET : UNSET);
        }
    }

    private Entry<Integer, MappedScriptValue> getEntry(Object item) {
        for (Entry<Integer, MappedScriptValue> entry : getScriptMapping(
                Global).gameValueMapping.entrySet()) {
            Items<?> items = entry.getValue().items();
            for (Item<?> i : items) {
                if (i.item == item) {
                    return entry;
                }
            }
        }
        return null;
    }
}
