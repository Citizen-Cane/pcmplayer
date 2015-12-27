package pcm.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import pcm.controller.ScriptCache;
import pcm.controller.ScriptParser;
import teaselib.Actor;
import teaselib.TeaseLib;

public class Script extends AbstractAction {
    public final Actor actor;

    public final String name;
    public Color backColor;
    public Color textColor;
    public ActionRange onAllSet = null;
    public ActionRange onClose = null;
    public ActionRange onRecognitionRejected = null;
    public String imageDirectory = null;
    public ActionRange startRange = null;

    public final Actions actions = new Actions();
    public final Map<Integer, AskItem> askItems = new HashMap<Integer, AskItem>();
    public final Map<Integer, MenuItem> menuItems = new HashMap<Integer, MenuItem>();
    public List<ActionRange> conditionRanges = null;

    public String mistressImages = null;

    public int gag = 0;

    private final ScriptCache scriptCache;
    public final Stack<ActionRange> stack;

    public Script(Actor actor, String name, ScriptCache scriptCache,
            BufferedReader reader) throws ParseError, ValidationError,
            IOException {
        this.actor = actor;
        this.name = name;
        this.scriptCache = scriptCache;
        this.stack = scriptCache.stack;
        ScriptParser parser = new ScriptParser(reader);
        TeaseLib.instance().log.info("Parsing script " + name);
        try {
            parser.parse(this);
            Action action = null;
            while ((action = parser.parseAction(this)) != null) {
                actions.put(action.number, action);
            }
        } catch (ParseError e) {
            if (e.script == null) {
                e.script = this;
            }
            throw e;
        }
        completeScriptDefaults();
        completeConditionRanges();
    }

    private void completeScriptDefaults() {
        if (responses == null) {
            responses = new HashMap<Statement, String>();
            if (!responses.containsKey(Statement.YesText)) {
                responses.put(Statement.YesText, "Yes, Miss");
            }
            if (!responses.containsKey(Statement.NoText)) {
                responses.put(Statement.NoText, "No, Miss");
            }
            if (!responses.containsKey(Statement.ResumeText)) {
                responses.put(Statement.ResumeText, "Ready, Miss");
            }
            if (!responses.containsKey(Statement.StopText)) {
                responses.put(Statement.StopText, "Please stop, Miss");
            }
            if (!responses.containsKey(Statement.CumText)) {
                responses.put(Statement.CumText, "I came, Miss");
            }
        }
    }

    /**
     * Allow the evaluation algorithm to just iterate through the collection
     */
    private void completeConditionRanges() {
        if (conditionRanges != null) {
            // To sort out all optional conditions in the last step
            conditionRanges.add(new ActionRange(Integer.MIN_VALUE,
                    Integer.MAX_VALUE));
            // make hasNext() check work...
            conditionRanges.add(null);
        }
    }

    public Script load(String name) throws ParseError, ValidationError,
            IOException {
        return scriptCache.get(actor, name);
    }

    @Override
    public void add(ScriptLineTokenizer cmd) {
        Statement name = cmd.statement;
        if (name == Statement.Restore) {
            // Restore is always executed, but
            // without a save state, restore is a noop
        } else if (name == Statement.BackColor) {
            String args[] = cmd.args();
            backColor = new Color(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else if (name == Statement.TextColor) {
            String args[] = cmd.args();
            backColor = new Color(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else if (name == Statement.SsDir) {
            imageDirectory = cmd.allArgs().replace('\\', '/');
            mistressImages = imageDirectory;
        } else if (name == Statement.AskTitle) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    cmd.allAsTextFrom(1));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), AskItem.ALWAYS, cmd.allAsTextFrom(2));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck2) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                    cmd.allAsTextFrom(3));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.Menu) {
            String args[] = cmd.args();
            MenuItem menuItem = new MenuItem(Integer.parseInt(args[0]),
                    new ActionRange(Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])), cmd.allAsTextFrom(3));
            menuItems.put(menuItem.n, menuItem);
        } else if (name == Statement.OnAllSet) {
            String args[] = cmd.args();
            onAllSet = args.length > 1 ? new ActionRange(
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.OnClose) {
            String args[] = cmd.args();
            onClose = args.length > 1 ? new ActionRange(
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Range) {
            String args[] = cmd.args();
            startRange = args.length > 1 ? new ActionRange(
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Message) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Debug) {
            // Ignored since debug flags are handled by the the player, not by
            // the scripts
        } else if (name == Statement.OnRecognitionRejected) {
            String args[] = cmd.args();
            onRecognitionRejected = args.length > 1 ? new ActionRange(
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Gag) {
            String args[] = cmd.args();
            gag = Integer.parseInt(args[0]);
        } else if (name == Statement.ConditionRange) {
            if (conditionRanges == null) {
                conditionRanges = new Vector<ActionRange>();
            }
            String args[] = cmd.args();
            int start = Integer.parseInt(args[0]);
            if (args.length > 1) {
                int end = Integer.parseInt(args[1]);
                conditionRanges.add(new ActionRange(start, end));
            } else {
                conditionRanges.add(new ActionRange(start));
            }
        } else {
            super.add(cmd);
        }
    }

    public String getResponseText(Statement name) throws ScriptExecutionError {
        if (responses.containsKey(name)) {
            return responses.get(name);
        } else {
            throw new ScriptExecutionError("Default text missing for " + name,
                    this);
        }
    }

    public void validate(List<ValidationError> validationErrors) {
        if (startRange == null) {
            validationErrors.add(new ValidationError("Missing start range",
                    this));
        } else if (!startRange.validate()) {
            validationErrors
                    .add(new ValidationError("Wrong start range", this));
        }
        // TeaseLib.resources().exists(imageDirectory);
        // if (!new File(root + ).exists)
    }

}
