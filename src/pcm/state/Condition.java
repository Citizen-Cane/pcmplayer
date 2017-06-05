package pcm.state;

import pcm.model.ConditionRange;
import pcm.state.persistence.ScriptState;

public interface Condition {

    boolean isTrueFor(ScriptState state);

    boolean isInside(ConditionRange conditionRange);
}
