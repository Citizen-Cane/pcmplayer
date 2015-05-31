package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Delay implements Visual {
	public final int from;
	public final int to;

	public Delay(int delay) {
		from = to = delay;
	}

	public Delay(int from, int to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void render(Player player) {
		player.setDuration(player.random(from, to));
	}
}
