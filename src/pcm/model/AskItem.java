package pcm.model;

public class AskItem {
    public final int n;
    public final int action;
    public final int condition;
    public final String message;

    public final static int ALWAYS = -1;

    AskItem(int n, String message) {
        this.n = n;
        this.action = 0;
        this.condition = ALWAYS;
        this.message = message;
    }

    AskItem(int n, int action, int condition, String message) {
        this.n = n;
        this.action = action;
        this.condition = condition;
        this.message = message;
    }
}
