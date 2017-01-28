package pcm.state.persistence;

import java.util.Collections;

import teaselib.util.Item;
import teaselib.util.Items;

public class MappedScriptItemValue<T> implements MappedScriptValue {

    private final int n;
    private final Items<T> items;

    public MappedScriptItemValue(int n, Item<T> item) {
        this.n = n;
        this.items = new Items<T>(Collections.singletonList(item));
    }

    public MappedScriptItemValue(int n, Items<T> items) {
        this.n = n;
        this.items = items;
    }

    @Override
    public int getNumber() {
        return n;
    }

    @Override
    public boolean isSet() {
        return items.available().size() > 0;
    }

    @Override
    public void set() {
        if (items.size() == 1) {
            // 1:1 mapping
            items.get(0).setAvailable(true);
        } else {
            throw new IllegalStateException(n + "(" + items.toString() + ")"
                    + ": Multiple-mapped values can only be unset");
        }
    }

    @Override
    public void unset() {
        for (Item<T> item : items) {
            item.setAvailable(false);
        }
    }

    @Override
    public Items<T> items() {
        return items;
    }
}
