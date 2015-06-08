package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Image implements Visual {
    public static final String IMAGES = "images/";

    public final String name;

    public Image(String path) {
        this.name = path;
    }

    @Override
    public void render(Player player) {
        player.setImage(IMAGES + name);
    }
}
