package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;

public class Message implements Visual {

	teaselib.text.Message message;
	
	
	public Message(teaselib.text.Message message) {
		this.message = message;
	}

	@Override
	public void render(Player player) {
		player.say(message);
	}
}
