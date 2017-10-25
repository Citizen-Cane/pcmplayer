package pcm.state.conditions;

import java.util.Arrays;
import java.util.HashSet;

import pcm.model.ConditionRange;
import pcm.state.Condition;

/**
 * @author Citizen-Cane
 *
 */
public abstract class ActionSetCondition extends HashSet<Integer> implements Condition {
    private static final long serialVersionUID = 1L;

    public ActionSetCondition(Integer... values) {
        addAll(Arrays.asList(values));
    }

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        for (Integer n : this) {
            if (conditionRange.contains(n)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this.getClass() != other.getClass())
            return false;
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

}
