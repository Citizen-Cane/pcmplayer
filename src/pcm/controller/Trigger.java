package pcm.controller;

import java.util.Set;

import pcm.model.Action;

public interface Trigger extends BreakPoint {
    @Override
    void reached();

    boolean expected();

    String getMessage();

    Set<Action> actions();
}
