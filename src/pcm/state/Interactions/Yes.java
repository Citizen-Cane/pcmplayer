package pcm.state.Interactions;

import teaselib.Answer;

public class Yes extends AbstractPause {
    public Yes(String text) {
        super(Answer.yes(text));
    }
}
