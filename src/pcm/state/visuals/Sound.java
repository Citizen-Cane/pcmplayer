package pcm.state.visuals;

import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.state.ValidatableResources;
import pcm.state.Visual;

public class Sound implements Visual, ValidatableResources {
    public static final String SOUNDS = "sounds/";

    public final String path;

    public Sound(String path) {
        this.path = SOUNDS + path;
    }

    @Override
    public void render(Player player) {
        player.setBackgroundSound(path);
    }

    @Override
    public List<String> resources() {
        return Collections.singletonList(path);
    }
}
