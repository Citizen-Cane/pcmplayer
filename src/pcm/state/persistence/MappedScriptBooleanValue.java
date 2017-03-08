package pcm.state.persistence;

import teaselib.core.TeaseLib;
import teaselib.util.Items;

public class MappedScriptBooleanValue implements MappedScriptValue {

    private final int n;

    private TeaseLib.PersistentBoolean item;

    public MappedScriptBooleanValue(int n, TeaseLib.PersistentBoolean item) {
        this.n = n;
        this.item = item;
    }

    @Override
    public int getNumber() {
        return n;
    }

    @Override
    public boolean isSet() {
        return item.isTrue();
    }

    @Override
    public void set() {
        item.set();
    }

    @Override
    public void unset() {
        item.clear();
    }

    @Override
    public Items<?> items() {
        return new Items<Object>();
    }

}
