package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class NoImage implements Visual {
	public static final NoImage instance = new NoImage();

	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.setImage(null);
	}
}
