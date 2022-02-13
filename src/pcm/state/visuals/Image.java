package pcm.state.visuals;

import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.state.ValidatableResources;
import pcm.state.Visual;

public class Image implements Visual, ValidatableResources {
    public static final String IMAGES = "/Mine/images/";

    public final String path;

    public Image(String path) {
        this.path = IMAGES + path;
    }

    @Override
    public void render(Player player) {
        player.setImage(path);
    }

    @Override
    public List<String> resources() {
        return Collections.singletonList(path);
    }

}
