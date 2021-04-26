package pcm.controller;

import java.util.Objects;

import pcm.model.Action;
import pcm.state.interactions.Stop;

/**
 * Run the given lambda when the checkpoint has been reached
 * 
 * @author Citizen-Cane
 *
 */
public class LambdaTrigger extends BasicTrigger {
    private final Runnable runnable;
    private boolean reached = false;

    public LambdaTrigger(Action action, Runnable runnable) {
        super("", action.number);

        Objects.requireNonNull(action);
        Objects.requireNonNull(runnable);

        if (action.interaction instanceof Stop) {
            throw new IllegalArgumentException(getClass().getName() + " is incompatible with stop interaction");
        }
        this.runnable = runnable;
    }

    @Override
    public void reached() {
        runnable.run();
        reached = true;
    }

    @Override
    public boolean suspend() {
        return false;
    }

    @Override
    public boolean expected() throws AssertionError {
        return reached;
    }
}
