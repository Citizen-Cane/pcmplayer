package pcm.controller;

import java.util.List;

import pcm.model.Action;

@Deprecated
public abstract class ProbabilityModelFirstApproach extends ProbabilityModel {
    public ProbabilityModelFirstApproach() {
        super();
    }

    @Override
    public void buildAccumulatedWeights(List<Action> actions) {
        double sum = 0.0;
        double normalized = 100.0;
        for (int i = 0; i < accumulatedWeights.length; i++) {
            Action a = actions.get(i);
            Integer weight = a.poss;
            double relativeWeight = normalized / accumulatedWeights.length;
            sum += weight != null ? weight
                    // This would be mathematically correct
                    // if none of the actions specified a "poss" value
                    : relativeWeight;
            accumulatedWeights[i] = sum;
            addPossValueForLogging((int) relativeWeight);
        }
        // Normalize and build interval
        for (int i = 0; i < accumulatedWeights.length; i++) {
            accumulatedWeights[i] *= normalized / sum;
        }
    }
}
