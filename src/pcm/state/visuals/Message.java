package pcm.state.visuals;

import pcm.state.Visual;
import teaselib.TeaseScript;

public class Message implements Visual {

	teaselib.text.Message message;
	
	
	public Message(teaselib.text.Message message) {
		this.message = message;
	}

	@Override
	public void render(TeaseScript teaseScript) {
		teaseScript.say(message);
	}
}
