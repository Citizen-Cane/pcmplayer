package pcm.model;

public class AskItem {
    public final int n;
    public final int action;
    public final int condition;
    public final String title;

    public final static int ALWAYS = -1;

    AskItem(int n, String title) {
        this.n = n;
        this.action = 0;
        this.condition = ALWAYS;
        this.title = title;
    }

    AskItem(int n, int action, int condition, String title) {
        this.n = n;
        this.action = action;
        this.condition = condition;
        this.title = title;
    }
}
