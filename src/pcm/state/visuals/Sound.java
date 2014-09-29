package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Sound implements Visual {
	public final String name;

	public Sound(String name) {
		this.name = name;
	}

	@Override
	public void render(Player player) {
		player.playSound(name);
	}

}
