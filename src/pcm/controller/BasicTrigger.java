package pcm.controller;

public abstract class BasicTrigger implements Trigger {
    private final int action;
    private final String message;

    protected BasicTrigger(String message, int action) {
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
    public String toString() {
        return "action=" + action;
    }

}
