package pcm.controller;

public interface BreakPoint {
    public static BreakPoint STOP = new BreakPoint() {
        @Override
        public boolean suspend() {
            return true;
        }
    };

    public static BreakPoint NONE = new BreakPoint() {
        @Override
        public boolean suspend() {
            return false;
        }
    };

    boolean suspend();
}