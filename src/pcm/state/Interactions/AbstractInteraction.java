package pcm.state.Interactions;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.state.Interaction;
import teaselib.core.speechrecognition.SpeechRecognitionResult.Confidence;

public abstract class AbstractInteraction implements Interaction {

    protected Confidence getConfidence(final Action action) {
        return action.visuals
                .containsKey(Statement.relaxedSpeechRecognitionConfidence)
                        ? Confidence.Default.lower() : Confidence.Default;
    }
}
