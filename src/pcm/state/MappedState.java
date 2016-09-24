/**
 * 
 */
package pcm.state;

import java.util.HashMap;
import java.util.Map;

import pcm.controller.Player;
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
    private final Map<Integer, Items<Toys>> toyMapping = new HashMap<Integer, Items<Toys>>();
    private final Map<Integer, teaselib.State> stateMapping = new HashMap<Integer, teaselib.State>();
    private final Map<Integer, teaselib.State> stateExpiredMapping = new HashMap<Integer, teaselib.State>();

    public MappedState(Player player) {
        super(player);
    }

    public void addMapping(Integer action, Item<Toys> item) {
        Items<Toys> items = new Items<Toys>();
        items.add(item);
        toyMapping.put(action, items);
    }

    public void addToyMapping(Integer action, Items<Toys> items) {
        toyMapping.put(action, items);
    }

    public void addStateMapping(Integer action, teaselib.State state) {
        stateMapping.put(action, state);
    }

    public void addStateExpiredMapping(Integer action, teaselib.State state) {
        stateExpiredMapping.put(action, state);
    }

    @Override
    public Long get(Integer n) {
        if (hasToyMapping(n)) {
            if (toyMapping.get(n).available().size() > 0) {
                super.set(n);
                return SET;
            } else {
                super.unset(n);
                return UNSET;
            }
        } else if (hasStateMapping(n)) {
            if (stateMapping.get(n).applied()) {
                super.set(n);
                return SET;
            } else {
                super.unset(n);
                return UNSET;
            }
        } else if (hasStateExpiredMapping(n)) {
            if (stateExpiredMapping.get(n).expired()) {
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
        return toyMapping.containsKey(n);
    }

    public Items<Toys> getMappedToys(Integer n) {
        return toyMapping.get(n);
    }

    private boolean hasStateMapping(Integer n) {
        return stateMapping.containsKey(n);
    }

    private boolean hasStateExpiredMapping(Integer n) {
        return stateExpiredMapping.containsKey(n);
    }

    /*
     * @see pcm.state.State#set(java.lang.Integer)
     */
    @Override
    public void set(Integer n) {
        if (hasToyMapping(n)) {
            Items<Toys> items = toyMapping.get(n);
            if (items.size() == 1) {
                // 1:1 mapping
                items.get(0).setAvailable(true);
            } else {
                throw new IllegalStateException(n + "(" + items.toString() + ")"
                        + ": Multiple-mapped values can only be unset");
            }
        } else if (hasStateMapping(n)) {
            stateMapping.get(n).apply();
        } else if (hasStateExpiredMapping(n)) {
            throw new IllegalStateException(
                    n + "(" + stateExpiredMapping.get(n).toString() + ")"
                            + ": Expiration time is read-only");
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
            Items<Toys> items = toyMapping.get(n);
            for (Item<Toys> item : items) {
                item.setAvailable(false);
            }
        } else if (hasStateMapping(n)) {
            stateMapping.get(n).remove();
        } else if (hasStateExpiredMapping(n)) {
            // TODO Called via ResetRange - no need to reset mappings,
            // but then .resetrange is correct from PCM view
            // throw new IllegalStateException(
            // n + "(" + stateExpiredMapping.get(n).toString() + ")"
            // + ": Expiration time is read-only");
        }
        super.unset(n);
    }
}
