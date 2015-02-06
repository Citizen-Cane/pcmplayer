package pcm.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import pcm.controller.ScriptCache;
import teaselib.TeaseLib;
import teaselib.image.ImageResourcesIterator;

public class Script extends AbstractAction {
    public final String name;
    public Color backColor;
    public Color textColor;
    public ActionRange onAllSet = null;
    public ActionRange onClose = null;
    public String imageDirectory = null;
    public ActionRange startRange = null;

    public Actions actions = new Actions();
    public Map<Integer, AskItem> askItems = new HashMap<Integer, AskItem>();
    public Map<Integer, MenuItem> menuItems = new HashMap<Integer, MenuItem>();

    public ImageResourcesIterator mistressImages = null;
    private static final String MISTRESS = "mistress/";

    private final ScriptCache scriptCache;
    public final Stack<ActionRange> stack;

    public Script(String name, ScriptCache scriptCache, BufferedReader reader)
            throws ParseError, ValidationError, IOException {
        this.name = name;
        this.scriptCache = scriptCache;
        this.stack = scriptCache.stack;
        ScriptParser parser = new ScriptParser(this, reader);
        TeaseLib.log("Parsing script " + name);
        try {
            parser.parseScript();
            Action action = null;
            while ((action = parser.parseAction()) != null) {
                actions.put(action.number, action);
            }
        } catch (ParseError e) {
            e.script = this;
            throw e;
        }
        completeScriptDefaults();
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

    public Script load(String name) throws ParseError, ValidationError,
            IOException {
        return scriptCache.get(name);
    }

    @Override
    public void add(ScriptLineTokenizer cmd) throws ParseError {
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
            String args[] = cmd.args();
            imageDirectory = allArgsFrom(args).replace('\\', '/');
            mistressImages = new ImageResourcesIterator(MISTRESS + "Vana/"
                    + imageDirectory);
        } else if (name == Statement.AskTitle) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    allArgsFrom(args, 1));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), AskItem.ALWAYS, allArgsFrom(
                            args, 2));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck2) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                    allArgsFrom(args, 3));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.Menu) {
            String args[] = cmd.args();
            MenuItem menuItem = new MenuItem(Integer.parseInt(args[0]),
                    new ActionRange(Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])), allArgsFrom(args, 3));
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
        } else {
            super.add(cmd);
        }
    }

    public String getResponseText(Statement name) throws ScriptExecutionError {
        if (responses.containsKey(name)) {
            return responses.get(name);
        } else {
            throw new ScriptExecutionError(this, "Default text missing for "
                    + name);
        }
    }

    public void validate(List<ValidationError> validationErrors) {
        if (startRange == null) {
            validationErrors.add(new ValidationError("Missing start range"));
        } else if (!startRange.validate()) {
            validationErrors.add(new ValidationError("Wrong start range"));
        }
        // TeaseLib.resources().exists(imageDirectory);
        // if (!new File(root + ).exists)
    }

}
