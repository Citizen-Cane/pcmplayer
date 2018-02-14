package pcm.state.Interactions;

import teaselib.Answer;

public class Pause extends AbstractPause {
    public Pause(String text) {
        super(Answer.resume(text));
    }
}
