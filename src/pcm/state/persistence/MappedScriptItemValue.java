package pcm.state.persistence;

import java.util.Iterator;

import teaselib.util.Item;
import teaselib.util.Items;

public class MappedScriptItemValue implements MappedScriptValue {
    private final int n;
    private final Items.Query items;

    public MappedScriptItemValue(int n, Items.Query items) {
        this.n = n;
        this.items = items;
    }

    @Override
    public int getNumber() {
        return n;
    }

    @Override
    public boolean isSet() {
        return items.anyAvailable();
    }

    @Override
    public void set() {
        Iterator<Item> item = items.inventory().iterator();
        if (!item.hasNext()) {
            // 1:1 mapping only
            throw new IllegalStateException(n + "(" + items.toString() + ")" + ": No item to set available");
        } else {
            item.next().setAvailable(true);
            if (item.hasNext()) {
                // 1:1 mapping only
                throw new IllegalStateException(
                        n + "(" + items.toString() + ")" + ": Multiple-mapped values can only be unset");
            }
        }
    }

    @Override
    public void unset() {
        for (Item item : items.inventory()) {
            item.setAvailable(false);
        }
    }

    @Override
    public Items items() {
        return items.inventory();
    }

}
