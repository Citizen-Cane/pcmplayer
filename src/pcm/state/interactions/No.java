package pcm.state.interactions;

import pcm.controller.Player;
import teaselib.Answer;

public class No extends AbstractPause {
    public No(String text) {
        super(Answer.no(text));
    }

    @Override
    protected void reply(Player player) {
        player.reply(answer);
    }
}
