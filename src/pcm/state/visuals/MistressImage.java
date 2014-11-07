package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.TeaseScript;

public class MistressImage implements Visual {
	public static final MistressImage instance = new MistressImage();

	private MistressImage()
	{}
	
	@Override
	public void render(Player player) {
		player.showImage(TeaseScript.MistressImage);
	}
}
