package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Sound implements Visual {
    public final String path;

    public Sound(String path) {
        this.path = path;
    }

    @Override
    public void render(Player player) {
        player.playSound(path);
    }
}
