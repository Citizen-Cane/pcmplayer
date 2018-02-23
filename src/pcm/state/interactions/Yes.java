package pcm.state.interactions;

import teaselib.Answer;

public class Yes extends AbstractPause {
    public Yes(String text) {
        super(Answer.yes(text));
    }
}
