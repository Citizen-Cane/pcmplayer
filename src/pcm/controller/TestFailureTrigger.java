package pcm.controller;

import java.util.Collections;
import java.util.Set;

import pcm.model.Action;

/**
 * @author Citizen-Cane
 *
 */
public class TestFailureTrigger extends BasicTrigger {

    public TestFailureTrigger(String message, Action action) {
        super(message, Collections.singleton(action));
    }

    public TestFailureTrigger(String message, Set<Action> actions) {
        super(message, actions);
    }

    @Override
    public void reached() {
        throw new IllegalStateException(actions() + ": " + getMessage());
    }

    @Override
    public boolean suspend() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean expected() {
        return false;
    }

}
