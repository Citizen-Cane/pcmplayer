package pcm.state.interactions;

import pcm.controller.Player;
import teaselib.Answer;

public class Pause extends AbstractPause {
    public Pause(String text) {
        super(Answer.resume(text));
    }

    @Override
    protected void reply(Player player) {
        player.reply(answer);
    }
}
