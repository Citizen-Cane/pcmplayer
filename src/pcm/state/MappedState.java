/**
 * 
 */
package pcm.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pcm.controller.Player;
import teaselib.persistence.Item;

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
    private final Player player;
    private final Map<Integer, List<Item>> mapping = new HashMap<Integer, List<Item>>();

    public MappedState(Player player, String root) {
        super(root, player.teaseLib);
        this.player = player;
    }

    public void addMapping(Integer action, Item item) {
        List<Item> items = new ArrayList<Item>();
        items.add(item);
        mapping.put(action, items);
    }

    public void addMapping(Integer action, List<Item> items) {
        mapping.put(action, items);
    }

    @Override
    public Integer get(Integer n) {
        if (mapping.containsKey(n)) {
            List<Item> items = mapping.get(n);
            boolean available = player.isAnyAvailable(items);
            if (available) {
                super.set(n);
            } else {
                super.unset(n);
            }
            return available ? SET : UNSET;
        } else {
            return super.get(n);
        }
    }

    public boolean hasMapping(Integer n) {
        return mapping.containsKey(n);
    }

    public List<Item> getMappedItems(Integer n) {
        return mapping.get(n);
    }

    /*
     * @see pcm.state.State#set(java.lang.Integer)
     */
    @Override
    public void set(Integer n) {
        if (hasMapping(n)) {
            List<Item> items = mapping.get(n);
            if (items.size() == 1) {
                // 1:1 mapping
                items.get(0).setAvailable(true);
            } else {
                throw new IllegalStateException(
                        "Mapped values can only be unset");
            }
        }
        super.set(n);
    }

    public void setOverride(Integer n) {
        if (hasMapping(n)) {
            super.set(n);
        } else {
            throw new IllegalStateException(
                    "setOverride can only be called for mappings");
        }
    }

    @Override
    public void unset(Integer n) {
        if (mapping.containsKey(n)) {
            List<Item> items = mapping.get(n);
            for (Item item : items) {
                item.setAvailable(false);
            }
        }
        super.unset(n);
    }

}
