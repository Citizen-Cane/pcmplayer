package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Image implements Visual {
	public final String name;
	
	public Image(String path) {
		this.name = path;
	}

	@Override
	public void render(Player player) {
		player.setImage(name);
	}
}
