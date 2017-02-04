package pcm.state.conditions;

import java.util.HashSet;

import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class MustNotAllOf extends HashSet<Integer> implements Condition {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isTrueFor(ScriptState state) {
        for (Integer mustnot : this) {
            if (state.get(mustnot).equals(ScriptState.UNSET)) {
                return true;
            }
        }
        return false;
    }
}
