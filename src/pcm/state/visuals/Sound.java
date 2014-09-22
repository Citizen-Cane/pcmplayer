package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class Sound implements Visual {
	public final String name;

	public Sound(String name) {
		this.name = name;
	}

	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.playSound(name);
	}

}
