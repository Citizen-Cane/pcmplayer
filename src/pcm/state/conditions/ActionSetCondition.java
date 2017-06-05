/**
 * 
 */
package pcm.state.conditions;

import java.util.HashSet;

import pcm.model.ConditionRange;
import pcm.state.Condition;

/**
 * @author Citizen-Cane
 *
 */
public abstract class ActionSetCondition extends HashSet<Integer> implements Condition {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        for (Integer n : this) {
            if (conditionRange.contains(n)) {
                return true;
            }
        }
        return false;
    }

}
