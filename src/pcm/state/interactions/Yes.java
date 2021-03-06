package pcm.state.interactions;

import pcm.controller.Player;
import teaselib.Answer;

public class Yes extends AbstractPause {
    public Yes(String text) {
        super(Answer.yes(text));
    }

    @Override
    protected void reply(Player player) {
        player.reply(answer);
    }
}
