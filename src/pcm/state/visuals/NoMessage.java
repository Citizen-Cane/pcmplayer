package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public final class NoMessage implements Visual {
    public static final NoMessage instance = new NoMessage();

    @Override
    public void render(Player player) {
        player.show((String) null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
