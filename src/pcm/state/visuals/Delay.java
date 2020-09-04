package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Delay implements Visual {
    public final int from;
    public final int to;

    public int duration = 0;

    public Delay(int delay) {
        from = to = delay;
    }

    public Delay(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void render(Player player) {
        duration = player.random.value(from, to);
        player.setDuration(duration);
    }
}
