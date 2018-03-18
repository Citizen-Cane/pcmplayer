package pcm.state.interactions;

import pcm.controller.Player;
import teaselib.Answer;

public class Chat extends AbstractPause {
    public Chat(String text) {
        super(Answer.yes(text));
    }

    @Override
    protected void reply(Player player) {
        player.chat(answer);
    }
}
