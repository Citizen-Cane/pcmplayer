package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class MistressImage implements Visual {
	public static final MistressImage instance = new MistressImage();

	private MistressImage()
	{}
	
	@Override
	public void render(TeaseScript teaseScript) {
		// TODO Should be automatic in TeaseScript
//		teaseScript.setImage(teaseScript.mistress.next());
	}
}
