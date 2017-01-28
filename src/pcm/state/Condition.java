package pcm.state;

import pcm.state.persistence.ScriptState;

public interface Condition {

	boolean isTrueFor(ScriptState state);
}
