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

    public void add(String line, String resourcePath) {
        if (message == null) {
            message = new Message(actor);
            messages.add(message);
        }
        Message.Part part = new Message.Part(line);
        if (part.type == Message.Type.Image) {
            message.add(part.type, resourcePath + Image.IMAGES + part.value);
        } else if (Message.Type.AudioTypes.contains(part.type)) {
            message.add(part.type, resourcePath + Sound.SOUNDS + part.value);
        } else if (part.type == Message.Type.DesktopItem) {
            message.add(part.type, resourcePath + part.value);
        } else {
            message.add(part);
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
