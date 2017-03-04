package pcm.state.persistence;

import teaselib.State;
import teaselib.util.Items;

public abstract class MappedScriptStateValue implements MappedScriptValue {
    private final int n;
    protected final State state;

    public MappedScriptStateValue(int n, teaselib.State state) {
        this.n = n;
        this.state = state;
    }

    @Override
    public int getNumber() {
        return n;
    }

    @Override
    public boolean isSet() {
        return state.applied();
    }

    @Override
    public void unset() {
        state.remove();
    }

    @Override
    public Items<?> items() {
        return new Items<teaselib.State>();
    }

    public static class ForSession extends MappedScriptStateValue {
        public ForSession(int n, State state) {
            super(n, state);
        }

        @Override
        public void set() {
            state.apply();
        }
    }

    public static class Indefinitely extends MappedScriptStateValue {
        public Indefinitely(int n, State state) {
            super(n, state);
        }

        @Override
        public void set() {
            state.apply();
            state.remember();
        }
    }
}
