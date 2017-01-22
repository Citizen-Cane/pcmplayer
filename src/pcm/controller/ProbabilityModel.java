package pcm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.Action;

public abstract class ProbabilityModel {
    private static final Logger logger = LoggerFactory
            .getLogger(ProbabilityModel.class);

    protected double accumulatedWeights[];
    private StringBuilder relativeWeights;

    public ProbabilityModel() {
    }

    public Action chooseActionBasedOnPossValue(List<Action> actions) {
        accumulatedWeights = new double[actions.size()];
        relativeWeights = new StringBuilder("     Weight:");
        logActions(actions);
        buildAccumulatedWeights(actions);
        logger.info(relativeWeights.toString());
        logAccumulatedWeights();
        return randomActionAccordingToWeight(actions);
    }

    private static void logActions(List<Action> actions) {
        StringBuilder actionList = null;
        for (Action a : actions) {
            int number = a.number;
            if (actionList == null) {
                actionList = new StringBuilder("     Action:\t" + number);
            } else {
                actionList.append("\t");
                actionList.append(number);
            }
        }
        if (actionList == null) {
            logger.info("Action list is empty");
        } else {
            logger.info(actionList.toString());
        }
    }

    protected abstract void buildAccumulatedWeights(List<Action> actions);

    protected abstract double random(double from, double to);

    private Action randomActionAccordingToWeight(List<Action> actions) {
        double value = random(0, 100.0);
        Action action = chooseAction(actions, value);
        if (action == null) {
            throw new IllegalStateException(
                    "Poss weighting calculation error - no action selected for random value "
                            + value);
        }
        logger.info(
                "Random = " + value + " -> choosing action " + action.number);
        return action;
    }

    private Action chooseAction(List<Action> actions, double weightedRandomValue) {
        Action action = null;
        for (int i = 0; i < accumulatedWeights.length; i++) {
            if (weightedRandomValue <= accumulatedWeights[i]) {
                action = actions.get(i);
                break;
            }
        }
        return action;
    }

    protected void addPossValueForLogging(int possValue) {
        String v = Integer.toString(possValue);
        relativeWeights.append("\t");
        relativeWeights.append(v);
    }

    public void logAccumulatedWeights() {
        StringBuilder weightList = null;
        for (int i = 0; i < accumulatedWeights.length; i++) {
            String w = String.format("%.2f", accumulatedWeights[i]);
            if (weightList == null) {
                weightList = new StringBuilder("Accumulated:\t" + w);
            } else {
                weightList.append("\t");
                weightList.append(w);
            }
        }
        // Log weights
        if (weightList != null) {
            logger.info(weightList.toString());
        } else {
            logger.info("Weight list is empty");
        }
    }
}
