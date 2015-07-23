package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.text.Message;

public class MistressImage implements Visual {
    public static final MistressImage instance = new MistressImage();

    private MistressImage() {
    }

    @Override
    public void render(Player player) {
        player.setImage(Message.DominantImage);
    }
}
