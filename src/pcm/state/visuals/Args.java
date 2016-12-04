package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Args implements Visual {

    public static final Args None = new Args();

    public final String[] args;

    public Args(String... args) {
        this.args = args;
    }

    @Override
    public void render(Player player) {
        // ignore
    }
}
