package pcm.model;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.ScriptCache;
import pcm.controller.ScriptParser;
import pcm.state.commands.Restore;
import teaselib.Actor;

public class Script extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(Script.class);

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

    /**
     * The condition range used when the script doesn't define its own list of
     * condition ranges.
     */
    private final static ActionRange DefaultConditionRange = new ActionRange(
            Integer.MIN_VALUE, Integer.MAX_VALUE);

    public Script(Actor actor, String name, ScriptCache scriptCache,
            ScriptParser parser)
            throws ScriptParsingException, ValidationIssue, IOException {
        this.actor = actor;
        this.name = name;
        this.scriptCache = scriptCache;
        this.stack = scriptCache.stack;
        logger.info("Parsing script " + name);
        try {
            parser.parse(this);
            Action action = null;
            while ((action = parser.parseAction(this)) != null) {
                actions.put(action.number, action);
            }
        } catch (ScriptParsingException e) {
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
            // No defaults since hard-coded messages can't be translated
        }
    }

    /**
     * Allow the evaluation algorithm to just iterate through the collection
     */
    private void completeConditionRanges() {
        if (conditionRanges != null) {
            // To sort out all optional conditions in the last step
            conditionRanges.add(DefaultConditionRange);
        } else {
            conditionRanges = Collections.singletonList(DefaultConditionRange);
        }
    }

    public Script load(String name)
            throws ScriptParsingException, ValidationIssue, IOException {
        return scriptCache.get(actor, name);
    }

    @Override
    public void add(ScriptLineTokenizer cmd) {
        Statement name = cmd.statement;
        if (name == Statement.Restore) {
            addCommand(new Restore());
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
            mistressImages = imageDirectory + "/*.jpg";
        } else if (name == Statement.AskTitle) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    cmd.allAsTextFrom(1));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), AskItem.ALWAYS,
                    cmd.allAsTextFrom(2));
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
                            Integer.parseInt(args[2])),
                    cmd.allAsTextFrom(3));
            menuItems.put(menuItem.n, menuItem);
        } else if (name == Statement.OnAllSet) {
            String args[] = cmd.args();
            onAllSet = args.length > 1
                    ? new ActionRange(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.OnClose) {
            String args[] = cmd.args();
            onClose = args.length > 1
                    ? new ActionRange(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Range) {
            String args[] = cmd.args();
            startRange = args.length > 1
                    ? new ActionRange(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Message) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Debug) {
            // Ignored since debug flags are handled by the the player, not by
            // the scripts
        } else if (name == Statement.OnRecognitionRejected) {
            String args[] = cmd.args();
            onRecognitionRejected = args.length > 1
                    ? new ActionRange(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]))
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

    public String getResponseText(Statement name)
            throws ScriptExecutionException {
        if (responses.containsKey(name)) {
            return responses.get(name);
        } else {
            throw new ScriptExecutionException(
                    "Default text missing for " + name, this);
        }
    }

    public void validate(List<ValidationIssue> validationErrors) {
        if (startRange == null) {
            validationErrors
                    .add(new ValidationIssue("Missing start range", this));
        } else if (!startRange.validate()) {
            validationErrors
                    .add(new ValidationIssue("Wrong start range", this));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
