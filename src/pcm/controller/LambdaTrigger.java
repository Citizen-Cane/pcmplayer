package pcm.controller;

public class LambdaTrigger extends BasicTrigger {
    private final Runnable runnable;
    private boolean reached = false;

    public LambdaTrigger(int action, Runnable runnable) {
        super("", action);
        this.runnable = runnable;
    }

    @Override
    public void reached() {
        runnable.run();
        reached = true;
    }

    @Override
    public boolean suspend() {
        // TODO Player should call reached() directly
        reached();
        return false;
    }

    @Override
    public boolean assertExpected() throws AssertionError {
        return reached;
    }
}
