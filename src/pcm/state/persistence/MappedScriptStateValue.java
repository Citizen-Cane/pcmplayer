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
        private final Object what;

        public ForSession(int n, State state, Object what) {
            super(n, state);
            this.what = what;
        }

        @Override
        public void set() {
            state.apply(what);
        }
    }

    public static class Indefinitely extends MappedScriptStateValue {
        private final Object what;

        public Indefinitely(int n, State state, Object what) {
            super(n, state);
            this.what = what;
        }

        @Override
        public void set() {
            state.apply(what);
            state.remember();
        }
    }
}
