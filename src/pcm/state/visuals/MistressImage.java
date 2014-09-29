package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class MistressImage implements Visual {
	public static final MistressImage instance = new MistressImage();

	private MistressImage()
	{}
	
	@Override
	public void render(Player player) {
		// TODO Should be automatic in TeaseScript
//		teaseScript.setImage(teaseScript.mistress.next());
	}
}
