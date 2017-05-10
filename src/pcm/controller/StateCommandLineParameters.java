package pcm.controller;

import static pcm.controller.StateCommandLineParameters.Keyword.Apply;
import static pcm.controller.StateCommandLineParameters.Keyword.Over;
import static pcm.controller.StateCommandLineParameters.Keyword.Remember;
import static pcm.controller.StateCommandLineParameters.Keyword.Remove;
import static pcm.controller.StateCommandLineParameters.Keyword.To;

import java.util.List;
import java.util.concurrent.TimeUnit;

import pcm.model.DurationFormat;
import teaselib.State;
import teaselib.core.util.CommandLineParameters;
import teaselib.core.util.ReflectionUtils;

public class StateCommandLineParameters extends CommandLineParameters<StateCommandLineParameters.Keyword> {
    private static final long serialVersionUID = 1L;

    public enum Keyword {
        Apply,
        To,
        Over,
        Remember,
        Remove,
    }

    public StateCommandLineParameters(String[] args) {
        super(args, Keyword.values());
    }

    public List<Enum<?>> applyOptions() throws ClassNotFoundException {
        return ReflectionUtils.getEnums(get(Apply));
    }

    public Object[] toOptions() throws ClassNotFoundException {
        return ReflectionUtils.getEnums(get(To)).toArray();
    }

    public DurationFormat durationOption() {
        return containsKey(Over) ? new DurationFormat(get(Keyword.Over).get(0)) : null;
    }

    public boolean rememberOption() {
        return containsKey(Remember);
    }

    public List<Enum<?>> removeOptions() throws ClassNotFoundException {
        return ReflectionUtils.getEnums(get(Remove));
    }

    public void handleStateOptions(State.Options options, final DurationFormat duration, final boolean remember) {
        if (duration != null) {
            State.Persistence persistence = options.over(duration.toSeconds(), TimeUnit.SECONDS);
            if (remember) {
                persistence.remember();
            }
        } else if (remember) {
            options.remember();
        }
    }

}
