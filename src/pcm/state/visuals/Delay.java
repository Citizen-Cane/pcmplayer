package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

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
	public void render(TeaseScript teaseScript) {
		teaseScript.delay(teaseScript.getRandom(from, to));
	}
}
