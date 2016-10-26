package pcm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pcm.model.Action;

/**
 * This probability model build bucket map, and calculates the accumulated
 * weight for each action based on its possibility value. The accumulated values
 * are normalized based on the sum of all values of all buckets, which is
 * equivalent to the sum of all poss value (including the values for unweighted
 * actions)
 * 
 * @author Citizen-Cane
 *
 */
public abstract class ProbabilityModelBasedOnPossBucketSum
        extends ProbabilityModel {
    public ProbabilityModelBasedOnPossBucketSum() {
        super();
    }

    @Override
    public void buildAccumulatedWeights(List<Action> actions) {
        Map<Integer, Integer> weightMap = buildWeightBucketMap(actions);
        int unweighted = calculateUnweightedValue(weightMap);
        int weightSum = calculateSumOfAllWeightedActions(weightMap, unweighted);
        int sum = calculateAccumulatedWeights(actions, unweighted, weightSum);
        if (sum != weightSum)
            throw new IllegalStateException(
                    "Poss weighting calculation error: sum " + sum + " != "
                            + "sum of all weighted actions " + weightSum);
    }

    private int calculateAccumulatedWeights(List<Action> actions,
            int unweighted, int weightSum) {
        int sum = 0;
        for (int i = 0; i < accumulatedWeights.length; i++) {
            Action a = actions.get(i);
            int d = a.poss != null ? a.poss : unweighted;
            sum += d;
            accumulatedWeights[i] = sum * 100 / weightSum;
            addPossValueForLogging(d);
        }
        return sum;
    }

    private static Map<Integer, Integer> buildWeightBucketMap(
            List<Action> actions) {
        Map<Integer, Integer> weightMap = new HashMap<Integer, Integer>();
        for (Action action : actions) {
            if (weightMap.containsKey(action.poss)) {
                weightMap.put(action.poss, weightMap.get(action.poss) + 1);
            } else {
                weightMap.put(action.poss, 1);
            }
        }
        return weightMap;
    }

    private static int calculateUnweightedValue(
            Map<Integer, Integer> weightMap) {
        int unweighted = 100;
        if (weightMap.containsKey(null)) {
            for (Entry<Integer, Integer> entry : weightMap.entrySet()) {
                if (entry.getKey() != null) {
                    unweighted -= entry.getKey();
                }
            }
        } else {
            unweighted = 0;
        }
        return unweighted;
    }

    private static int calculateSumOfAllWeightedActions(
            Map<Integer, Integer> weightMap, int unweighted) {
        int weightSum = 0;
        for (Entry<Integer, Integer> entry2 : weightMap.entrySet()) {
            Integer key = entry2.getKey();
            if (key != null) {
                weightSum += key * entry2.getValue();
            } else {
                weightSum += unweighted * entry2.getValue();
            }
        }
        return weightSum;
    }
}
