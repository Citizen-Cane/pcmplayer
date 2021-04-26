package pcm.controller;

/**
 * @author Citizen-Cane
 *
 */
public class TestFailureTrigger extends BasicTrigger {

    public TestFailureTrigger(String message, int action) {
        super(message, action);
    }

    @Override
    public void reached() {
        throw new IllegalStateException(getAction() + ": " + getMessage());
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
