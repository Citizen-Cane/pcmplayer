package pcm.state.conditions;

import pcm.state.persistence.ScriptState;

public class MustAnyOf extends ActionSetCondition {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isTrueFor(ScriptState state) {
        for (Integer must : this) {
            if (state.get(must).equals(ScriptState.SET)) {
                return true;
            }
        }
        return false;
    }
}
