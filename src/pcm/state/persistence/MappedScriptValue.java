package pcm.state.persistence;

import teaselib.util.Items;

public interface MappedScriptValue {

    public int getNumber();

    public boolean isSet();

    public void set();

    public void unset();

    Items items();
}
