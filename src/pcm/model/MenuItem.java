package pcm.model;

public class MenuItem {
    public final int n;
    public final ActionRange range;
    public final String message;

    MenuItem(int n, ActionRange range, String message) {
        this.n = n;
        this.range = range;
        this.message = message;
    }

    @Override
    public String toString() {
        return message + ": " + range;
    }
}
