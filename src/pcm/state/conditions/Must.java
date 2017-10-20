package pcm.state.conditions;

import pcm.state.persistence.ScriptState;

public class Must extends ActionSetCondition {
    private static final long serialVersionUID = 1L;

    public Must(Integer... values) {
        super(values);
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        for (Integer must : this) {
            if (!state.get(must).equals(ScriptState.SET)) {
                return false;
            }
        }
        return true;
    }
}
