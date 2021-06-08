package pcm.controller;

import java.util.Set;

import pcm.model.Action;

public abstract class BasicTrigger implements Trigger {
    private final Set<Action> actions;
    private final String message;

    protected BasicTrigger(String message, Set<Action> actions) {
        super();
        this.message = message;
        this.actions = actions;
    }

    @Override
    public Set<Action> actions() {
        return actions;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "actions=" + actions;
    }

}
