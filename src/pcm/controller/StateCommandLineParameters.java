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
import teaselib.core.util.QualifiedItem;

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
            if (items().length == 0) {
                throw new IllegalArgumentException("Items must be specified first");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Not an enum", e);
        }
    }

    public DurationFormat durationOption() {
        return containsKey(Over) ? new DurationFormat(get(Keyword.Over).get(0)) : null;
    }

    public boolean rememberOption() {
        return containsKey(Remember);
    }

    public String[] items() throws ClassNotFoundException {
        return array(defaultKeyword);
    }

    public String[] array(Keyword keyword) throws ClassNotFoundException {
        List<String> items = get(keyword);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    @Override
    public List<String> get(Object value) {
        List<String> values = super.get(value);
        if (declarations != null && declarations.available()) {
            check(values);
        }
        return values;
    }

    private void check(List<String> values) {
        for (String value : values) {
            for (java.util.Map.Entry<String, String> entry : declarations.entries()) {
                if (QualifiedItem.fromType(value).namespace().equalsIgnoreCase(entry.getKey())) {
                    if (entry.getValue().equals(Declarations.ENUM)) {
                        // TODO try to instanciate enum
                        // TODO move method to declarations
                    }
                }
            }
        }
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

    public QualifiedItem<?>[] toQualifiedItems(Object... items) {
        QualifiedItem<?>[] qualifiedItems = new QualifiedItem<?>[items.length];
        for (int i = 0; i < items.length; i++) {
            qualifiedItems[i] = QualifiedItem.fromType(items[i]);
        }
        return qualifiedItems;
    }

    public QualifiedItem<?>[] toQualifiedItems(List<?> items) {
        QualifiedItem<?>[] qualifiedItems = new QualifiedItem<?>[items.size()];
        for (int i = 0; i < items.size(); i++) {
            qualifiedItems[i] = QualifiedItem.fromType(items.get(i));
        }
        return qualifiedItems;
    }
}
