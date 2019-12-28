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
    public Items items() {
        return Items.None;
    }

    public static class ForSession extends MappedScriptStateValue {
        private final Enum<?>[] peers;

        public ForSession(int n, State state, Enum<?>... peers) {
            super(n, state);
            this.peers = peers;
        }

        @Override
        public void set() {
            state.applyTo((Object[]) peers);
        }

        @Override
        public void unset() {
            state.remove();
        }
    }

    public static class Indefinitely extends MappedScriptStateValue {
        private final Enum<?>[] peers;

        public Indefinitely(int n, State state, Enum<?>... peers) {
            super(n, state);
            this.peers = peers;
        }

        @Override
        public void set() {
            state.applyTo((Object[]) peers).remember();
        }

        @Override
        public void unset() {
            state.remove();
        }
    }

}
