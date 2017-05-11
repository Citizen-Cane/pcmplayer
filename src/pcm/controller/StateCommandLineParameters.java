package pcm.controller;

import static pcm.controller.StateCommandLineParameters.Keyword.Over;
import static pcm.controller.StateCommandLineParameters.Keyword.Remember;
import static pcm.controller.StateCommandLineParameters.Keyword.Remove;

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

        Is,
        Available,
        CanApply,
        Applied,
        Expired,

        ;
        public static boolean isCommand(String[] args) {
            Enum<?> keyword = getKeyword(args[0], Keyword.values());
            return keyword == Apply || keyword == Remove;
        }
    }

    public StateCommandLineParameters(String[] args) {
        super(args, Keyword.values());
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

    public Enum<?>[] leading() throws ClassNotFoundException {
        List<Enum<?>> enums = ReflectionUtils.getEnums(getLeading());
        Enum<?>[] array = new Enum<?>[enums.size()];
        return enums.toArray(array);
    }

    public Enum<?>[] options(Keyword keyword) throws ClassNotFoundException {
        List<Enum<?>> enums = ReflectionUtils.getEnums(get(keyword));
        Enum<?>[] array = new Enum<?>[enums.size()];
        return enums.toArray(array);
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
