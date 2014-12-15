package pcm.state.visuals;

import java.util.Vector;

import pcm.controller.Player;
import pcm.state.Visual;

public class Message implements Visual {

    Vector<String> message;

    public Message(String text) {
        this.message = new Vector<>();
        add(text);
    }

    public void add(String text) {
        message.add(text);
    }

    @Override
    public void render(Player player) {
        String[] array = new String[message.size()];
        player.say(message.toArray(array));
    }

    public Vector<String> getMessage() {
        return message;
    }
}
