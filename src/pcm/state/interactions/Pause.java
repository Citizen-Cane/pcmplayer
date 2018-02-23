package pcm.state.interactions;

import teaselib.Answer;

public class Pause extends AbstractPause {
    public Pause(String text) {
        super(Answer.resume(text));
    }
}
