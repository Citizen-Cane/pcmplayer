package pcm.state.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.model.AbstractAction;
import pcm.model.IllegalStatementException;
import pcm.state.BasicCommand;
import pcm.state.commands.KeyReleaseCommandLineParameters.Keyword;
import pcm.state.persistence.ScriptState;
import teaselib.Features;
import teaselib.core.devices.release.KeyReleaseSetup;
import teaselib.util.Item;
import teaselib.util.Items;

/**
 * @author Citizen-Cane
 *
 */
public class KeyReleaseCommand extends BasicCommand {
    static final Logger logger = LoggerFactory.getLogger(KeyReleaseCommand.class);

    private final KeyReleaseCommandLineParameters args;

    public KeyReleaseCommand(KeyReleaseCommandLineParameters args) {
        super(statement(args));
        this.args = args;
    }

    static ParameterizedStatement statement(final KeyReleaseCommandLineParameters args) {
        String[] items = args.items(Keyword.Item);
        if (items.length == 0) {
            items = args.items(Keyword.Prepare);
        }
        Declarations declarations = args.getDeclarations();
        declarations.validate(items, Item.class);

        if (args.containsKey(KeyReleaseCommandLineParameters.Keyword.Prepare)) {
            return prepare(args, items);
        } else {
            throw new IllegalStatementException("Keyword not found", args);
        }
    }

    private static ParameterizedStatement prepare(KeyReleaseCommandLineParameters args, String[] items) {
        return new ParameterizedStatement(AbstractAction.Statement.KeyRelease, args) {
            @Override
            public void run(ScriptState state) {
                Player player = state.player;
                Items lockableItems = player.items(items).prefer(Features.Lockable);
                player.interaction(KeyReleaseSetup.class).prepare(lockableItems, player::show);
                // TODO review all scripts to prepare a little in advance
            }
        };
    }

    @Override
    public String toString() {
        return args.toString();
    }

}
