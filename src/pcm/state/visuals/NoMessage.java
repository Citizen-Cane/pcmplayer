package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class NoMessage implements Visual {
	public static final NoMessage instance = new NoMessage();

	@Override
	public void render(Player player) {
		player.say((String)null);
	}
}
