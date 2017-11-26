package pcm.controller;

public interface Trigger extends BreakPoint {
    void reached();

    boolean assertExpected() throws AssertionError;

    String getMessage();

    int getAction();
}
