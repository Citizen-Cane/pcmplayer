package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.TeaseScript;

public class NoImage implements Visual {
	public static final NoImage instance = new NoImage();

	@Override
	public void render(Player player) {
		player.setImage(TeaseScript.NoImage);
	}
}
