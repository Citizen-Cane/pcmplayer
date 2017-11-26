package pcm.controller;

/**
 * @author Citizen-Cane
 *
 */
public class CheckPointTrigger extends BasicTrigger {
    private final boolean expected;
    private boolean reached = false;

    public CheckPointTrigger(String message, int action, boolean expected) {
        super(message, action);
        this.expected = expected;
    }

    @Override
    public void reached() {
        reached = true;
    }

    @Override
    public boolean suspend() {
        reached();
        return false;
    }

    @Override
    public boolean assertExpected() throws AssertionError {
        return expected == reached;
    }

    @Override
    public String toString() {
        return super.toString() + " reached=" + reached + " expected=" + expected;
    }

}
