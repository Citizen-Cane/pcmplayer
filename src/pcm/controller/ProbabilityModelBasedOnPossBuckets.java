package pcm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pcm.model.Action;

/**
 * This probability model calculates the poss value for unweighted actions, then
 * use the sum of all values in each bucket.
 * 
 * @author Citizen-Cane
 *
 */
@Deprecated
public abstract class ProbabilityModelBasedOnPossBuckets
        extends ProbabilityModel {
    public ProbabilityModelBasedOnPossBuckets() {
        super();
    }

    @Override
    public void buildAccumulatedWeights(List<Action> actions) {
        double sum = 0.0;
        // From the Quick Pins game in mine-nipt one can assume that
        // - all weighted actions account for their weight
        // - all unweighted actions get a weight 100 - sum(set(weighted))
        // where the set contains each value just once
        // -> all actions with poss n should together weight exactly n
        // so a single action of weight n counts as n/occurence(n)
        // and m unweighted actions as 100-sum(set(n))/m
        Map<Integer, Double> weightMap = new HashMap<Integer, Double>();
        for (Action action : actions) {
            if (weightMap.containsKey(action.poss)) {
                weightMap.put(action.poss, weightMap.get(action.poss) + 1.0);
            } else {
                weightMap.put(action.poss, 1.0);
            }
        }
        double unweighted = 100.0;
        if (weightMap.containsKey(null)) {
            for (Entry<Integer, Double> entry : weightMap.entrySet()) {
                if (entry.getKey() != null) {
                    unweighted -= entry.getKey();
                }
            }
        } else {
            unweighted = 0.0;
        }

        // The weight map
        // p1*n1 + p2*n2 + ...

        // What we want
        // 100 = w1*n1 + w2*n2 + ...

        // i=1
        // 100 = w1*n1 => w1 = 100/n1

        // expand all of the weight map to 100
        int weightSum = 0;
        for (Entry<Integer, Double> entry2 : weightMap.entrySet()) {
            Integer key = entry2.getKey();
            if (key != null) {
                weightSum += key;
            } else {
                weightSum += unweighted;
            }
        }

        for (int i = 0; i < accumulatedWeights.length; i++) {
            Action a = actions.get(i);
            Double occurences = weightMap.get(a.poss);
            double d = (a.poss != null ? ((double) a.poss) : unweighted)
                    / occurences * 100.0 / weightSum;
            sum += d;
            accumulatedWeights[i] = sum;
            addPossValueForLogging((int) d);
        }
        if (Math.abs(sum - 100.0) > 0.000001)
            throw new IllegalStateException("Poss weighting calculation error");
    }
}
