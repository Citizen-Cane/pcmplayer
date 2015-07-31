package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.Message;

public class NoImage implements Visual {
    public static final NoImage instance = new NoImage();

    @Override
    public void render(Player player) {
        player.setImage(Message.NoImage);
    }
}
