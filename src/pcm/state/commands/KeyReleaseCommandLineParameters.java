package pcm.state.commands;

import java.util.Arrays;
import java.util.List;

import pcm.controller.Declarations;
import teaselib.core.util.CommandLineParameters;

public class KeyReleaseCommandLineParameters extends CommandLineParameters<KeyReleaseCommandLineParameters.Keyword> {
    private static final long serialVersionUID = 1L;

    private Declarations declarations;

    public enum Keyword {
        Item,
        Prepare
    }

    public KeyReleaseCommandLineParameters(CommandLineParameters<Keyword> args, Declarations declarations) {
        super(args, Keyword.class);
        this.declarations = declarations;
        validateArgs();
    }

    public KeyReleaseCommandLineParameters(String[] args, Declarations declarations) {
        this(Arrays.asList(args), declarations);
    }

    public KeyReleaseCommandLineParameters(List<String> args, Declarations declarations) {
        super(args, Keyword.values(), Keyword.class);
        this.declarations = declarations;
        validateArgs();
    }

    private void validateArgs() {
        if (items(Keyword.Prepare).length == 0 && items(Keyword.Item).length == 0) {
            throw new IllegalArgumentException("No items specified");
        }
    }

    public String[] items(Keyword keyword) {
        List<String> items = get(keyword);
        declarations.validate(items);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public Declarations getDeclarations() {
        return declarations;
    }

}
