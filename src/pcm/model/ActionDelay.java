package pcm.model;

public class ActionDelay {
    public final int from;
    public final int to;
    public final ActionRange stop;

    public ActionDelay(int delay) {
        this.from = this.to = delay;
        this.stop = null;
    }

    public ActionDelay(int from, int to) {
        this.from = from;
        this.to = to;
        this.stop = null;
    }

    public ActionDelay(int from, int to, ActionRange stop) {
        this.from = from;
        this.to = to;
        this.stop = stop;
    }

}
