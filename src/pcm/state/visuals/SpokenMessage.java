package pcm.state.visuals;

import java.util.List;
import java.util.Vector;

import pcm.controller.Player;
import pcm.state.Visual;

public class SpokenMessage implements Visual {

    List<List<String>> messages = new Vector<>();
    Vector<String> message = null;

    public SpokenMessage(String text) {
        this.messages = new Vector<>();
        add(text);
    }

    public void add(String text) {
        if (message == null) {
            message = new Vector<>();
            messages.add(message);
        }
        message.add(text);
    }

    public void newSection() {
        message = null;
    }

    @Override
    public void render(Player player) {
        for (List<String> message : messages) {
            String[] array = new String[message.size()];
            player.say(message.toArray(array));
        }
    }

    public List<List<String>> getParts() {
        return messages;
    }
}
