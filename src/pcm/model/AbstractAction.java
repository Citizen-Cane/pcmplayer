package pcm.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.state.Command;
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
         * Execute a range as a sub program
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
         * The action is considered only if all other actions in the range are
         * set or their conditions are all false
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
         * Specifies the action that is used for tracking the usage of a gag.
         * Controls display of prompts, as a gagged slave won't be able to use
         * speech recognition.
         */
        Gag,

        /**
         * Triggers if the specified number of actions in the given range are
         * set
         */
        NumberOfActionsSet,

        /**
         * Triggers if the given range contains at least {@code n} executable
         * actions.
         */
        NumActionsAvailable,

        /**
         * Like {@code Must}, but if no action is available, the range is
         * evaluated again, with the {@code Should} condition being ignored.
         * 
         * @see Statement#ConditionRange
         */
        Should,

        /**
         * Like {@code MustNot}, but if no action is available, the range is
         * evaluated again, with the {@code ShouldNot} condition being ignored.
         * 
         * @see Statement#ConditionRange
         */
        ShouldNot,

        /**
         * Can be added multiple times to a script to define a list of ranges
         * that are used when relaxing {@link Statement#Should} /
         * {@link Statement#ShouldNot} conditions.
         * <p>
         * Whenever an action contains {@link Statement#Should} /
         * {@link Statement#ShouldNot}, and evaluating the range does not yield
         * any executable actions, the evaluation is repeated with relaxable
         * conditions ignored, starting with the first entry. Entries are added
         * to the ignore list, and the evaluation is repeated until the
         * evaluation yields executable actions or all relaxable conditions are
         * ignored.
         * <p>
         * Evaluation takes place in the order defined in the script,conditions
         * in the first condition range are ignored first. Therefore, the most
         * important condition ranges must be defined last.
         */
        ConditionRange,

        /**
         * Inverse of {@link TimeFrom}. Triggers if the duration in the second
         * argument hasn't been reached yet.
         */
        TimeTo,

        /**
         * Control key release devices. This statement does nothing if the
         * device isn't present, so it can safely be scattered all over the
         * script when tying the slave up.
         * <p>
         * Just prepare the device when telling the slave to tie himself up.
         * Then start the device, enter sleep as appropriate, release the key at
         * the end of the script or wait until the device releases the key
         * automatically.
         */
        KeyRelease,

        /**
         * Relax speech recognition confidence to longer prompts to be spoken
         * and be recognized on the first try. As side effect the prompt might
         * be dismissed by background noise, so be careful and avoid this
         * statement if the user has to make final decisions.
         * <p>
         * If used right, it's a good tool for creating realistic dialogs.
         */
        relaxedSpeechRecognitionConfidence,

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

        ;

        public final static Map<String, Statement> lookup = new HashMap<String, Statement>();
        public final static Map<String, Statement> KeywordToStatement = new HashMap<String, Statement>();
    }

    {
        for (Statement name : EnumSet.allOf(Statement.class)) {
            Statement.lookup.put(name.toString().toLowerCase(), name);
        }
        Statement.KeywordToStatement.put("cum", Statement.CumText);
        Statement.KeywordToStatement.put("no", Statement.NoText);
        Statement.KeywordToStatement.put("resume", Statement.ResumeText);
        Statement.KeywordToStatement.put("stop", Statement.StopText);
        Statement.KeywordToStatement.put("yes", Statement.YesText);
    }

    public List<Command> commands = null;
    public Map<Statement, String> responses = null;

    public void addCommand(Command command) {
        if (this.commands == null) {
            this.commands = new Vector<Command>();
        }
        commands.add(command);
    }

    void addResponse(Statement statement, String response) {
        if (responses == null) {
            responses = new HashMap<Statement, String>();
        } else if (responses.containsKey(statement)) {
            throw new IllegalArgumentException("Duplicate default prompt: " + statement.toString());
        }
        responses.put(statement, response);
    }

    public ActionRange execute(ScriptState state) throws ScriptExecutionException {
        if (commands != null) {
            for (Command command : commands) {
                Logger logger = LoggerFactory.getLogger(command.getClass());
                logger.info(command.toString());
                command.execute(state);
            }
        }
        return null;
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

}
