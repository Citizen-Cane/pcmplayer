package pcm.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pcm.controller.Declarations;
import pcm.state.Command;
import pcm.state.Condition;
import pcm.state.commands.ResetRange;
import pcm.state.conditions.TimeFrom;
import pcm.state.persistence.ScriptState;

public abstract class AbstractAction {
    // / Names of Statements used in PCM scripts
    public enum Statement {
        // Conditions
        Must,
        MustNot,
        Poss,
        TimeFrom,
        NumActionsFrom,

        // Commands
        Set,
        UnSet,
        SetTime,
        ResetRange,
        SetRange,
        Repeat,
        RepeatAdd,
        RepeatDel,
        Save,

        // Next action
        Range,
        LoadSbd,
        Quit,

        // Script-global
        Debug,
        Restore,
        BackColor,
        TextColor,
        OnAllSet,
        OnClose,
        SsDir,

        // Script and action
        YesText,
        NoText,
        ResumeText,
        ChatText,

        // Visual
        NoImage,
        Image,
        Message,
        Txt,
        PlayWav,
        PlayAvi,
        Say,
        Exec,

        // Interaction
        YesNo,
        Pause,
        PopUp,
        Ask,

        // Menus
        AskTitle,
        AskCheck,
        Menu,

        // Obsolete
        Delay, // delay is based on text length,
        ActionDelay, // ActionDelay is used to specify absolute delay
        // TODO Remove delay 0, delay x from all over the script
        // Only keep delay xy STOP n, and explicit ActionDelay statements
        // TODO Also, .Delay 0 implies .NoImage -> can be removed in script,
        // but the .NoImage statement is needed in some situations

        // Add-Ons

        /**
         * Text for the "Cum" button in break statemetn
         */
        CumText,

        /**
         * Text for the stop button
         */
        StopText,

        /**
         * Displays break buttons while executing a given range
         */
        Break,

        /**
         * Displays the checkbox if the specified action number is set
         */
        AskCheck2,

        /**
         * Execute a range as a sub program.
         * <p>
         * This command effectively pushes the action range onto the stack, and executes the gosub range. On return,
         * execution continues with the action range of this action.
         * <p>
         * The gosub-statement effectively works as an action range provider, and is executed after all other statements
         * in the current action have been processed.
         */
        GoSub,

        /**
         * Return from a sub program and continue execution
         */
        Return,

        /**
         * Call a handler to comment rejected speech recognitions.
         */
        OnRecognitionRejected,

        /**
         * The action is considered only if all other actions in the range are set or their conditions are all false
         */
        Else,

        /**
         * Execute a command statement if an action is set
         */
        IfSet,

        /**
         * Execute a command statement if an action is not set
         */
        IfUnset,

        /**
         * Triggers if the specified number of actions in the given range are set
         */
        NumberOfActionsSet,

        /**
         * Triggers if the given range contains at least {@code n} executable actions.
         */
        NumActionsAvailable,

        /**
         * Like {@code Must}, but if no action is available, the range is evaluated again, with the {@code Should}
         * condition being ignored.
         * 
         * @see Statement#ConditionRange
         */
        Should,

        /**
         * Like {@code MustNot}, but if no action is available, the range is evaluated again, with the {@code ShouldNot}
         * condition being ignored.
         * 
         * @see Statement#ConditionRange
         */
        ShouldNot,

        /**
         * Can be added multiple times to a script to define a list of ranges that are used when relaxing
         * {@link Statement#Should} / {@link Statement#ShouldNot} conditions.
         * <p>
         * Whenever an action contains {@link Statement#Should} / {@link Statement#ShouldNot}, and evaluating the range
         * does not yield any executable actions, the evaluation is repeated with relaxable conditions ignored, starting
         * with the first entry. Entries are added to the ignore list, and the evaluation is repeated until the
         * evaluation yields executable actions or all relaxable conditions are ignored.
         * <p>
         * Evaluation takes place in the order defined in the script,conditions in the first condition range are ignored
         * first. Therefore, the most important condition ranges must be defined last.
         */
        ConditionRange,

        /**
         * Inverse of {@link TimeFrom}. Triggers if the duration in the second argument hasn't been reached yet.
         */
        TimeTo,

        /**
         * KeyRelease command [duration] Control key release devices. This statement does nothing if the device isn't
         * present, so it can safely be scattered all over the script when tying the slave up.
         * <p>
         * Just prepare the device when telling the slave to tie himself up. Then start the device, enter sleep as
         * appropriate, release the key at the end of the script or wait until the device releases the key
         * automatically.
         * <p>
         * Valid comamnds are: Prepare, Start, Sleep, Release
         */
        KeyRelease,

        /**
         * True if at least one of the actions is unset.
         */
        MustNotAllOf,

        /**
         * True if at least one of the actions is set.
         */
        MustAnyOf,

        /**
         * Handle {@link teaselib.State}
         */
        State,

        /**
         * Replace text in the script, much like with the C++ #define command
         */
        Define,

        /**
         * Handle {@link teaselib.Item}
         */
        Item,

        /**
         * Invert the condition
         */
        Not,

        /**
         * Declare a name space for qualified items
         */
        Declare,

        Yes,

        No,

        Chat,

        /**
         * The message will be appended to the previous message.
         */
        Append,

        /**
         * The next message will be appended to the current message.
         */
        Prepend,

        /**
         * The next message will replace the previous message. If a message has effectively been appended, only the last
         * message will be replaced.
         */
        Replace,

        /**
         * Include another script. Action numbers must match ascending order, to avoid errors.
         */
        Include,

        ;

        public static final Map<String, Statement> Lookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        public static final Map<String, Statement> KeywordToStatement = new HashMap<>();
    }

    {
        for (Statement name : EnumSet.allOf(Statement.class)) {
            Statement.Lookup.put(name.toString(), name);
        }
        Statement.KeywordToStatement.put("cum", Statement.CumText);
        Statement.KeywordToStatement.put("no", Statement.NoText);
        Statement.KeywordToStatement.put("resume", Statement.ResumeText);
        Statement.KeywordToStatement.put("chat", Statement.ChatText);
        Statement.KeywordToStatement.put("stop", Statement.StopText);
        Statement.KeywordToStatement.put("yes", Statement.YesText);
    }

    public List<Command> commands = null;
    public Map<Statement, String> responses = null;

    public void addCommand(Command command) {
        if (this.commands == null) {
            this.commands = new ArrayList<>();
        }
        commands.add(command);
    }

    void addResponse(Statement statement, String response) {
        if (responses == null) {
            responses = new EnumMap<>(Statement.class);
        } else if (responses.containsKey(statement)) {
            throw new IllegalArgumentException("Duplicate default prompt: " + statement.toString());
        }
        responses.put(statement, response);
    }

    public void execute(ScriptState state) throws ScriptExecutionException {
        state.execute(commands);
    }

    /**
     * @throws ScriptParsingException
     */
    public void add(ScriptLineTokenizer cmd) throws ScriptParsingException {
        Statement name = cmd.statement;
        if (name == Statement.YesText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.NoText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.ResumeText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.ChatText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.StopText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.CumText) {
            addResponse(name, cmd.allAsText());
        } else if (name == Statement.ResetRange) {
            String args[] = cmd.args();
            addCommand(new ResetRange(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
        } else {
            throw new UnsupportedOperationException("Statement ." + name.toString() + " not implemented");
        }
    }

    static Condition createConditionFrom(int lineNumber, String line, Script script, Declarations declarations)
            throws ScriptParsingException {
        ScriptLineTokenizer cmd = new ScriptLineTokenizer(lineNumber, line, script, declarations);
        Action action = new Action(0) {
            @Override
            public void execute(ScriptState state) throws ScriptExecutionException {
                throw new UnsupportedOperationException();
            }
        };
        action.add(cmd);
        return action.conditions.get(0);
    }

}
