package pcm.model;

import pcm.state.Condition;

public class StatementConditionRange implements ConditionRange {
    private final Condition conditionRange;

    public StatementConditionRange(Condition conditionRange) {
        this.conditionRange = conditionRange;
    }

    @Override
    public boolean contains(Object condition) {
        return condition instanceof Condition && conditionRange.equals(condition);
    }
}