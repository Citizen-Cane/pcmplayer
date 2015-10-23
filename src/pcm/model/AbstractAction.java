package pcm.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pcm.state.Command;
import pcm.state.State;
import pcm.state.commands.ResetRange;
import teaselib.TeaseLib;

public abstract class AbstractAction {

    // / Names of Statements used in PCM scripts
    public enum Statement {
        // Conditions
        Must,
        MustNot,
        Poss,
        TimeFrom,
        NumActionsFrom,
        NumberOfActionsSet,

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
         * Text for the Cum button in brreak statemetn
         */
        CumText,

        /**
         * Text for the stop button
         */
        StopText,

        /**
         * Displasy break buttons while executing a given range
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
        IfUnset, ;
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
        }
        if (responses.containsKey(statement)) {
            throw new IllegalArgumentException("Duplicate "
                    + statement.toString());
        }
        responses.put(statement, response);
    }

    public ActionRange execute(State state) throws ScriptExecutionError {
        if (commands != null) {
            for (Command command : commands) {
                TeaseLib.log(command.getClass().getSimpleName() + " "
                        + command.toString());
                command.execute(state);
            }
        }
        return null;
    }

    public void add(ScriptLineTokenizer cmd) {
        Statement name = cmd.statement;
        if (name == Statement.YesText) {
            addResponse(name, cmd.asText());
        } else if (name == Statement.NoText) {
            addResponse(name, cmd.asText());
        } else if (name == Statement.ResumeText) {
            addResponse(name, cmd.asText());
        } else if (name == Statement.StopText) {
            addResponse(name, cmd.asText());
        } else if (name == Statement.CumText) {
            addResponse(name, cmd.asText());
        } else if (name == Statement.ResetRange) {
            String args[] = cmd.args();
            addCommand(new ResetRange(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])));
        } else {
            throw new UnsupportedOperationException("Statement ."
                    + name.toString() + " not implemented");
        }
    }

    public String allArgsFrom(String[] args) {
        return allArgsFrom(args, 0);
    }

    public String allArgsFrom(String[] args, int n) {
        StringBuilder s = new StringBuilder();
        s.append(args[n]);
        for (int i = n + 1; i < args.length; i++) {
            s.append(" ");
            s.append(args[i]);
        }
        return s.toString();
    }
}
