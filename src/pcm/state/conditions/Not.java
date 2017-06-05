package pcm.state.conditions;

import pcm.model.ConditionRange;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;

public class Not implements Condition {
    final Condition condition;

    public Not(Condition condition) {
        super();
        this.condition = condition;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        return !condition.isTrueFor(state);
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
