package pcm.state.interactions;

import teaselib.Answer;

public class No extends AbstractPause {
    public No(String text) {
        super(Answer.no(text));
    }
}
