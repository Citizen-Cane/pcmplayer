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
import teaselib.text.Message;

public class SpokenMessage implements Visual, Validatable {

    List<List<String>> messages = new Vector<List<String>>();
    Vector<String> message = null;

    public SpokenMessage(String text) {
        this.messages = new Vector<List<String>>();
        add(text);
    }

    public void add(String text) {
        if (message == null) {
            message = new Vector<String>();
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
            player.say(message);
        }
    }

    public List<List<String>> getParts() {
        return messages;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationError> validationErrors) {
        // Use any actor, there are no actor specific parts in PCM
        Actor actor = new Actor(Actor.Dominant, "en-us");
        for (List<String> text : messages) {
            Message message = new Message(actor);
            for (String part : text) {
                try {
                    message.add(part);
                } catch (Exception e) {
                    validationErrors.add(new ValidationError(action, e));
                }
            }
        }

    }
}
