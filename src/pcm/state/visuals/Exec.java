package pcm.state.visuals;

import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.state.ValidatableResources;
import pcm.state.Visual;

public class Exec implements Visual, ValidatableResources {
    String path;

    public Exec(String path) {
        this.path = path;
    }

    @Override
    public void render(Player player) {
        player.showDesktopItem(path);
    }

    @Override
    public List<String> resources() {
        return Collections.singletonList(path);
    }

    @Override
    public String toString() {
        return path;
    }
}
