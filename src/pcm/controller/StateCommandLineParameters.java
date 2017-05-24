package pcm.controller;

import static pcm.controller.StateCommandLineParameters.Keyword.Over;
import static pcm.controller.StateCommandLineParameters.Keyword.Remember;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pcm.model.DurationFormat;
import teaselib.State;
import teaselib.core.util.CommandLineParameters;

public class StateCommandLineParameters extends CommandLineParameters<StateCommandLineParameters.Keyword> {
    private static final long serialVersionUID = 1L;
    private Declarations declarations;

    public enum Keyword {
        Item,

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
        Remaining,

        GreaterThan,
        LessOrEqualThan,

        ;

        static final Set<Keyword> COMMANDS = new HashSet<Keyword>(Arrays.asList(Apply, Remove));

        public static boolean isCommand(String[] args) {
            Enum<?> keyword = getKeyword(args[0], Keyword.values());
            return keyword == Apply || keyword == Remove;
        }
    }

    public boolean isCommand() {
        for (Keyword keyword : Keyword.COMMANDS) {
            if (containsKey(keyword)) {
                return true;
            }
        }
        return false;
    }

    public StateCommandLineParameters(String[] args, Declarations declarations) {
        super(args, Keyword.values());
        this.declarations = declarations;
        try {
            if (items(Keyword.Item).length == 0) {
                throw new IllegalArgumentException("Items must be specified first");
            } else if (items(Keyword.Remove).length > 0) {
                throw new IllegalArgumentException("Remove can may only remove all items");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Not an enum", e);
        }

    }

    public String[] items(Keyword keyword) throws ClassNotFoundException {
        List<String> items = declarations.checked(get(keyword));
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String item(Keyword keyword) throws ClassNotFoundException {
        List<String> items = declarations.checked(get(keyword));
        if (items.size() == 1) {
            return items.get(0);
        } else {
            throw new IllegalArgumentException(keyword + " requires a signle argument");
        }
    }

    public String[] values(Keyword keyword) {
        List<String> items = get(keyword);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String value(Keyword keyword) {
        List<String> items = get(keyword);
        if (items.size() == 1) {
            return items.get(0);
        } else {
            throw new IllegalArgumentException(keyword + " requires a signle argument");
        }
    }

    public DurationFormat durationOption() {
        return containsKey(Over) ? new DurationFormat(get(Keyword.Over).get(0)) : null;
    }

    public boolean rememberOption() {
        return containsKey(Remember);
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
