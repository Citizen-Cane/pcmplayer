package pcm.controller;

public interface Trigger extends BreakPoint {
    @Override
    void reached();

    boolean expected();

    String getMessage();

    int getAction();
}
