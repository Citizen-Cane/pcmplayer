package pcm.state.conditions;

import pcm.state.persistence.ScriptState;

public class MustNot extends ActionSetCondition {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isTrueFor(ScriptState state) {
        for (Integer mustnot : this) {
            if (state.get(mustnot).equals(ScriptState.SET)) {
                return false;
            }
        }
        return true;
    }
}
