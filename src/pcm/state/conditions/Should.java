package pcm.state.conditions;

import pcm.model.ConditionRange;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class Should implements Condition {
    public final Condition condition;

    public Should(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        return condition.isTrueFor(state);
    }

    @Override
    public String toString() {
        return condition.getClass().getSimpleName() + " " + condition.toString();
    }

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        return condition.isInside(conditionRange);
    }

}
