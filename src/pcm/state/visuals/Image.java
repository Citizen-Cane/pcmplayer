package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class Image implements Visual {
	public final String name;
	
	public Image(String path) {
		this.name = path;
	}

	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.setImage(name);
	}
}
