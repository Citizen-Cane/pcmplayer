package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class NoMessage implements Visual {
	public static final NoMessage instance = new NoMessage();

	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.say((String)null);
	}
}
