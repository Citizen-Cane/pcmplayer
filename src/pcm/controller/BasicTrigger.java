package pcm.controller;

public abstract class BasicTrigger implements Trigger {
    private final int action;
    private final String message;

    public BasicTrigger(String message, int action) {
        super();
        this.message = message;
        this.action = action;
    }

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean suspend() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reached() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean assertExpected() throws AssertionError {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return "action=" + action;
    }

}
