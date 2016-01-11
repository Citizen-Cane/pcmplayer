package pcm.state.visuals;

import java.util.List;
import java.util.Vector;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ValidationError;
import pcm.state.Validatable;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.Message;

public class SpokenMessage implements Visual, Validatable {

    private final List<Message> messages = new Vector<Message>();
    private Message message = null;
    private final Actor actor;

    public SpokenMessage(Actor actor) {
        this.actor = actor;
    }

    public void add(String line) {
        if (message == null) {
            message = new Message(actor);
            messages.add(message);
        }
        Message.Type type = Message.determineType(line);
        if (type == Message.Type.Image) {
            message.add(type, Image.IMAGES + line);
        } else if (type == Message.Type.Sound) {
            message.add(type, Sound.SOUNDS + line);
        } else {
            message.add(type, line);
        }
    }

    public void newSection() {
        message = null;
    }

    public void end() {
        for (Message message : messages) {
            message.joinSentences().readAloud();
        }
    }

    @Override
    public void render(Player player) {
        for (Message message : messages) {
            player.say(message);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        // Nothing to do anymore, since all messages have been parsed already
    }
}
