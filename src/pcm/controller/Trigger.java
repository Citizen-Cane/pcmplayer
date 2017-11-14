package pcm.controller;

/**
 * @author Citizen-Cane
 *
 */
public class Trigger implements BreakPoint {
    public final String message;
    public final int action;
    public final boolean expected;

    public boolean actual = false;

    public Trigger(String assertion, int action, boolean expected) {
        this.message = assertion;
        this.action = action;
        this.expected = expected;
    }

    @Override
    public boolean suspend() {
        actual = true;
        return false;
    }
}
