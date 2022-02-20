package pcm.model;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.ScriptCache;
import pcm.controller.ScriptParser;
import pcm.state.Condition;
import pcm.state.commands.Restore;
import pcm.state.interactions.AbstractPause;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.Txt;
import teaselib.Actor;
import teaselib.ActorImages.Next;
import teaselib.core.devices.release.Actuator;

public class Script extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(Script.class);

    public final Actor actor;

    public final String name;
    public final String scriptApplyAttribute;

    public Color backColor;
    public Color textColor;
    public ActionRange onAllSet = null;
    public ActionRange onClose = null;
    public ActionRange onRecognitionRejected = null;
    public String imageDirectory = null;
    public ActionRange startRange = null;

    public final Actions actions = new Actions();
    public final Map<Integer, AskItem> askItems = new HashMap<>();
    public final Map<Integer, MenuItem> menuItems = new HashMap<>();
    public List<ConditionRange> conditionRanges = null;

    public String mistressImages = null;

    private final ScriptCache scriptCache;
    public final Deque<Action> stack;

    private Actuator keyReleaseActuator = null;

    public final StatementCollectors.Factory collectorFactory;

    /**
     * The condition range used when the script doesn't define its own list of condition ranges.
     */
    public static final ConditionRange DefaultConditionRange = condition -> true;

    public Script(Actor actor, String name, ScriptCache scriptCache, ScriptParser parser)
            throws ScriptParsingException, ValidationIssue, IOException {
        this.actor = actor;
        this.name = name;
        this.scriptApplyAttribute = "Applied.by." + name;
        this.scriptCache = scriptCache;
        this.stack = scriptCache.stack;

        collectorFactory = new StatementCollectors.Factory();
        collectorFactory.add(Statement.Txt, txtCollector());
        Supplier<StatementCollector> messageCollector = messageCollector();
        collectorFactory.add(Statement.Message, messageCollector);
        collectorFactory.add(Statement.Prepend, messageCollector);
        collectorFactory.add(Statement.Append, messageCollector);
        collectorFactory.add(Statement.Replace, messageCollector);

        logger.info("Parsing script {}", name);
        parser.parse(this);
        Action action = null;
        while ((action = parser.parseAction(this)) != null) {
            actions.put(action.number, action);
        }

        completeScriptDefaults();
        completeConditionRanges();

    }

    private Supplier<StatementCollector> txtCollector() {
        return () -> new StatementCollector() {
            Txt txt;

            @Override
            public void init() {
                txt = new Txt(actor);
            }

            @Override
            public void parse(ScriptLineTokenizer cmd) {
                String text = cmd.line.substring(Statement.Txt.toString().length() + 1);
                txt.add(text.trim());
            }

            @Override
            public void nextSection(Action action) { // Nothing to do
            }

            @Override
            public void applyTo(Action action) {
                txt.end();
                action.message = txt;
            }
        };
    }

    private Supplier<StatementCollector> messageCollector() {
        return () -> new StatementCollector() {
            SpokenMessage message;

            @Override
            public void init() { // Nothing to do
                message = new SpokenMessage(actor);
            }

            @Override
            public void parse(ScriptLineTokenizer cmd) {
                if (cmd.statement == Statement.Message) {
                    message.add(cmd.line);
                } else if (SpokenMessage.MODIFIERS.contains(cmd.statement)) {
                    message.setModifier(cmd.statement);
                } else {
                    throw new IllegalStatementException(cmd.statement, cmd.allArgs());
                }
            }

            @Override
            public void nextSection(Action action) {
                if (action.interaction instanceof AbstractPause) {
                    AbstractPause pause = (AbstractPause) action.interaction;
                    message.completeSection(pause.answer);
                    action.interaction = null;
                } else if (action.interaction == null) {
                    message.completeSection();
                } else {
                    throw new IllegalArgumentException("Action " + action.number + ": " + "Interaction "
                            + action.interaction.getClass().getSimpleName() + " not allowed as message section prompt");
                }
                message.startNewSection();
            }

            @Override
            public void applyTo(Action action) {
                message.completeMessage();
                action.message = message;
            }
        };
    }

    private void completeScriptDefaults() {
        if (responses == null) {
            responses = Collections.emptyMap();
            // No defaults since hard-coded messages can't be translated
        }
    }

    /**
     * Allow the evaluation algorithm to just iterate through the collection
     */
    private void completeConditionRanges() {
        if (conditionRanges != null) {
            // To eventually relax all optional conditions
            conditionRanges.add(DefaultConditionRange);
        } else {
            conditionRanges = Collections.singletonList(DefaultConditionRange);
        }
    }

    public Script load(String name) throws ScriptParsingException, ValidationIssue, IOException {
        return scriptCache.get(actor, name);
    }

    @Override
    public void add(ScriptLineTokenizer cmd) throws ScriptParsingException {
        Statement name = cmd.statement;
        if (name == Statement.Restore) {
            addCommand(new Restore());
        } else if (name == Statement.BackColor) {
            backColor = color(cmd.args());
        } else if (name == Statement.TextColor) {
            textColor = color(cmd.args());
        } else if (name == Statement.SsDir) {
            imageDirectory = cmd.allArgs().replace('\\', '/');
            mistressImages = imageDirectory + "/*.jpg";
            actor.images.advance(Next.Scene, imageDirectory);
        } else if (name == Statement.AskTitle) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]), cmd.allAsTextFrom(1));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]), Integer.parseInt(args[1]), AskItem.ALWAYS,
                    cmd.allAsTextFrom(2));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.AskCheck2) {
            String args[] = cmd.args();
            AskItem askItem = new AskItem(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]), cmd.allAsTextFrom(3));
            askItems.put(askItem.n, askItem);
        } else if (name == Statement.Menu) {
            String args[] = cmd.args();
            MenuItem menuItem = new MenuItem(Integer.parseInt(args[0]),
                    new ActionRange(Integer.parseInt(args[1]), Integer.parseInt(args[2])), cmd.allAsTextFrom(3));
            menuItems.put(menuItem.n, menuItem);
        } else if (name == Statement.OnAllSet) {
            String args[] = cmd.args();
            onAllSet = args.length > 1 ? new ActionRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.OnClose) {
            String args[] = cmd.args();
            onClose = args.length > 1 ? new ActionRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Range) {
            String args[] = cmd.args();
            startRange = args.length > 1 ? new ActionRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.Message) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Debug) {
            // Ignored since debug flags are handled by the the player, not by
            // the scripts
        } else if (name == Statement.OnRecognitionRejected) {
            String args[] = cmd.args();
            onRecognitionRejected = args.length > 1
                    ? new ActionRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]))
                    : new ActionRange(Integer.parseInt(args[0]));
        } else if (name == Statement.ConditionRange) {
            if (conditionRanges == null) {
                conditionRanges = new ArrayList<>();
            }
            String args[] = cmd.args();
            if (Statement.Lookup.containsKey(args[0].substring(1))) {
                final Condition condition = createConditionFrom(cmd.lineNumber, cmd.allArgs(), cmd.script,
                        cmd.declarations);
                conditionRanges.add(new StatementConditionRange(condition));
            } else {
                int start = Integer.parseInt(args[0]);
                if (args.length > 1) {
                    int end = Integer.parseInt(args[1]);
                    conditionRanges.add(new ActionRange(start, end));
                } else {
                    conditionRanges.add(new ActionRange(start));
                }
            }
        } else {
            super.add(cmd);
        }
    }

    private static Color color(String[] args) {
        return new Color(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    public void clearKeyReleaseActuator() {
        keyReleaseActuator = null;
    }

    public void setKeyReleaseActuator(Actuator actuator) {
        keyReleaseActuator = actuator;
    }

    public Optional<Actuator> getKeyReleaseActuator() {
        return Optional.ofNullable(keyReleaseActuator);
    }

    public String getResponseText(Statement name) {
        if (responses.containsKey(name)) {
            return responses.get(name);
        } else {
            throw new NoSuchElementException("Default text missing for " + name);
        }
    }

    public void validate(List<ValidationIssue> validationErrors) {
        if (startRange == null) {
            validationErrors.add(new ValidationIssue(this, "Missing start range"));
        } else if (!startRange.validate()) {
            validationErrors.add(new ValidationIssue(this, "Wrong start range"));
        }
    }

    @Override
    public String toString() {
        return "Script " + name;
    }
}
