package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Sound implements Visual {
    public static final String SOUNDS = "sounds/";

    public final String path;

    public Sound(String path) {
        this.path = path;
    }

    @Override
    public void render(Player player) {
        player.setBackgroundSound(SOUNDS + path);
    }
}
