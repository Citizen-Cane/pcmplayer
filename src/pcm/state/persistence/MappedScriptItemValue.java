package pcm.state.persistence;

import java.util.Collections;
import java.util.Iterator;

import teaselib.util.Item;
import teaselib.util.Items;

public class MappedScriptItemValue implements MappedScriptValue {
    private final int n;
    private final Items items;

    public MappedScriptItemValue(int n, Item item) {
        this.n = n;
        this.items = new Items(Collections.singletonList(item));
    }

    public MappedScriptItemValue(int n, Items items) {
        this.n = n;
        this.items = items;
    }

    public MappedScriptItemValue(int n, Items... items) {
        this.n = n;
        this.items = new Items();
        for (Items i : items) {
            this.items.addAll(i);
        }
    }

    @Override
    public int getNumber() {
        return n;
    }

    @Override
    public boolean isSet() {
        return !items.getAvailable().isEmpty();
    }

    @Override
    public void set() {
        Iterator<Item> item = items.iterator();
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
        for (Item item : items) {
            item.setAvailable(false);
        }
    }

    @Override
    public Items items() {
        return items;
    }
}
