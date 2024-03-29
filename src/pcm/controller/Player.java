package pcm.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.AbstractAction;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ConditionRange;
import pcm.model.LoadSbdRange;
import pcm.model.Script;
import pcm.model.ScriptException;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import pcm.state.Command;
import pcm.state.Condition;
import pcm.state.Visual;
import pcm.state.commands.DebugEntry;
import pcm.state.commands.ResetRange;
import pcm.state.conditions.Should;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.ScriptState;
import pcm.state.visuals.Image;
import teaselib.Actor;
import teaselib.Config;
import teaselib.MainScript;
import teaselib.Message;
import teaselib.Sexuality.Gender;
import teaselib.Sexuality.Sex;
import teaselib.TeaseScript;
import teaselib.core.InstructionalImages;
import teaselib.core.ResourceLoader;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.TeaseLib;
import teaselib.core.media.MediaRenderer;
import teaselib.core.texttospeech.ScriptScanner;
import teaselib.core.texttospeech.TextToSpeechRecorder;
import teaselib.core.texttospeech.Voice;
import teaselib.host.Host;
import teaselib.util.SceneBasedImages;
import teaselib.util.SpeechRecognitionRejectedScript;
import teaselib.util.TextVariables;

/**
 * @author Citizen-Cane
 * 
 */
public class Player extends TeaseScript implements MainScript {
    static final Logger logger = LoggerFactory.getLogger(Player.class);

    public static final String ScriptFolder = "scripts/";

    public final String namespaceApplyAttribute;
    public final MappedScriptState state;
    public final BreakPoints breakPoints;

    private final ScriptCache scripts;
    private final String mistressPath;
    private final String mainScript;

    private final ProbabilityModel probabilityModel = new ProbabilityModelBasedOnPossBucketSum() {
        @Override
        public double random(double from, double to) {
            return Player.this.random.value(from, to);
        }
    };

    public Script script = null;
    public Action action = null;
    public ActionRange playRange = null;

    public boolean validateScripts = false;
    public boolean debugOutput = false;

    private boolean invokedOnAllSet = false;
    private final AtomicReference<Host.ScriptInterruptedEvent.Reason> scriptInterruptedReason = new AtomicReference<>();

    /**
     * The EndAction tells the player to hand over execution from the PCM script engine back to the caller.
     * <p>
     * Push it on the stack, and call {@link Player#playFrom(ActionRange)}. The current script is executed up to the
     * next occurrence of a {@link AbstractAction.Statement#Return} statement.
     * <p>
     * The quit command also returns this action.
     */
    public static final Action EndAction = new Action(0) {
        @Override
        public void execute(ScriptState state) throws ScriptExecutionException {
            throw new UnsupportedOperationException();
        }
    };

    public static void recordVoices(Actor actor, File path, String projectName, String[] scriptNames, String[] assets)
            throws IOException, ValidationIssue, ScriptParsingException, InterruptedException, ExecutionException {
        ResourceLoader resources = new ResourceLoader(path, projectName);
        resources.addAssets(assets);
        try (TextToSpeechRecorder recorder = new TextToSpeechRecorder(path, projectName, resources,
                new TextVariables())) {
            Symbols dominantSubmissiveRelations = Symbols.getDominantSubmissiveRelations();
            for (Entry<String, String> entry : dominantSubmissiveRelations.entrySet()) {
                Symbols dominantSubmissiveRelation = new Symbols();
                dominantSubmissiveRelation.put(entry.getKey(), entry.getValue());
                ScriptCache scripts = new ScriptCache(resources, Player.ScriptFolder, dominantSubmissiveRelation);
                recorder.startPass(entry.getKey(), entry.getValue());
                for (String scriptName : scriptNames) {
                    Script script = scripts.get(actor, scriptName);
                    ScriptScanner scriptScanner = new PCMScriptScanner(script);
                    recorder.run(scriptScanner);
                }
            }
            recorder.finish();
        }
    }

    public Player(TeaseLib teaseLib, ResourceLoader resources, Actor actor, String namespace, String mistressPath,
            String mainscript) {
        super(teaseLib, resources, actor, namespace);
        this.namespaceApplyAttribute = "Applied.by." + namespace;
        this.state = new MappedScriptState(this);
        this.breakPoints = new BreakPoints();

        Symbols symbols = createDominantSubmissiveSymbols();
        this.scripts = new ScriptCache(resources, Player.ScriptFolder, symbols);
        this.mistressPath = mistressPath;

        boolean haveImages = mistressPath != null;
        if (haveImages) {
            // TODO assign each scene to an activity script as before -> use script name as chapter or hint
            // -> befire it was mistressPath + script.mistressImages
            actor.images = new SceneBasedImages(resources(mistressPath + "*.jpg"));
            // TODO naming scheme is too short for pose cache - need at least project name as root folder
            // - the assets are stored this way, it's a Mine organization issue
            // -> change folder names in Mine or added code to SceneBasedImages
        }
        actor.instructions = new InstructionalImages(resources(Image.IMAGES + "*.jpg"));

        mainScript = mainscript;
    }

    private Symbols createDominantSubmissiveSymbols() {
        var dominantSubmissiveSymbol = new StringBuilder();

        if (actor.gender == Voice.Male) {
            dominantSubmissiveSymbol.append("M");
        } else if (actor.gender == Voice.Female) {
            dominantSubmissiveSymbol.append("F");
        } else {
            throw new IllegalArgumentException(actor.toString());
        }

        if (persistence.newEnum(Sex.class).value() == Sex.Female) {
            dominantSubmissiveSymbol.append("f");
        } else {
            if (persistence.newEnum(Gender.class).value() == Gender.Feminine) {
                dominantSubmissiveSymbol.append("tv");
            } else {
                dominantSubmissiveSymbol.append("m");
            }
        }

        Symbols staticSymbols = new Symbols();
        staticSymbols.put(dominantSubmissiveSymbol.toString(), "true");
        return staticSymbols;
    }

    @Override
    public void run() {
        play(mainScript);
    }

    public void play(String scriptName) {
        StringTokenizer argv = parseDebugFile(scriptName);
        play(argv.nextToken(), parseStartRange(argv));
    }

    private StringTokenizer parseDebugFile(String scriptName) {
        String startRange = scriptName;
        String resourcePath = getClass().getSimpleName() + ResourceLoader.separator + "debug.txt";
        try {
            InputStream debugStream = resources.get(resourcePath);
            if (debugStream != null) {
                debugOutput = true;
                validateScripts = Boolean.parseBoolean(teaseLib.config.get(Config.Debug.StopOnAssetNotFound));
                try (var debugReader = new BufferedReader(new InputStreamReader(debugStream));) {
                    String line;
                    while ((line = debugReader.readLine()) != null) {
                        line = line.trim();
                        int comment = line.indexOf("//");
                        if (comment != 0) {
                            if (comment > 0) {
                                line = line.substring(0, comment - 1);
                            }
                            if (!line.isEmpty()) {
                                startRange = line;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(resourcePath, e);
        }
        return new StringTokenizer(startRange, " \t");
    }

    private static ActionRange parseStartRange(StringTokenizer argv) {
        final ActionRange range;
        if (argv.hasMoreElements()) {
            int start = Integer.parseInt(argv.nextToken());
            if (argv.hasMoreElements()) {
                int end = Integer.parseInt(argv.nextToken());
                range = new ActionRange(start, end);
            } else {
                range = new ActionRange(start);
            }
        } else {
            range = null;
        }
        return range;
    }

    public void play(String name, ActionRange startRange) {
        play(name, startRange, ActionRange.all);
    }

    public void play(String name, ActionRange startRange, ActionRange playRange) {
        SpeechRecognitionRejectedScript srRejectedHandler = actor.speechRecognitionRejectedScript;
        actor.speechRecognitionRejectedScript = pcmSpeechRecognitionRejectedScript();
        try {
            loadScript(name);
        } catch (ScriptException e) {
            reportError(e);
            return;
        } catch (Exception e) {
            reportError(e, name);
            return;
        }

        if (script != null) {
            try {
                play(startRange, playRange);
            } catch (ScriptInterruptedException e) {
                logger.debug(e.getMessage(), e);
            } catch (ScriptException e) {
                reportError(e);
            } catch (Exception e) {
                reportError(e, name);
            } finally {
                teaseLib.host.setQuitHandler(null);
                actor.speechRecognitionRejectedScript = srRejectedHandler;
            }
        }
    }

    public void playFrom(ActionRange startRange) throws ScriptExecutionException {
        play(startRange, ActionRange.all);
    }

    public void playOnly(ActionRange startRange) throws ScriptExecutionException {
        play(startRange, startRange);
    }

    public void play(ActionRange startRange, ActionRange playRange) throws ScriptExecutionException {
        if (script == null) {
            throw new IllegalStateException("No script loaded");
        }

        ActionRange range;
        if (startRange != null) {
            range = startRange;
        } else {
            range = script.startRange;
        }
        action = getAction(range);
        playRange(playRange);
    }

    private SpeechRecognitionRejectedScript pcmSpeechRecognitionRejectedScript() {
        return new SpeechRecognitionRejectedScript(this) {
            @Override
            public boolean canRun() {
                return script != null && script.onRecognitionRejected != null && scriptInterruptedReason.get() == null;
            }

            @Override
            public void run() {
                try {
                    // The rejected script must end with a return statement, and not continue somewhere else
                    Player.this.scripts.stack.push(EndAction);
                    action = getAction(Player.this.script.onRecognitionRejected);
                    Player.this.playRange(new ActionRange(0, Integer.MAX_VALUE));
                    // script is continued normally because playRange(...) restores the previous action on completion
                } catch (ScriptExecutionException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };
    }

    public void loadScript(String name)
            throws ScriptParsingException, ValidationIssue, IOException, ScriptExecutionException {
        setScript(scripts.get(actor, name));
        if (validateScripts) {
            validateProject();
        }
        resetScript();
    }

    private void setScript(Script newScript) {
        logger.info("Loading script {}", newScript.name);
        script = newScript;
        state.setScript(script);
    }

    public List<ValidationIssue> validateProject() throws ScriptParsingException, ValidationIssue, IOException {
        List<ValidationIssue> validationIssues = new ArrayList<>();

        validateAspects(script, state, validationIssues);
        for (String s : scripts.names()) {
            Script subScript = scripts.get(actor, s);
            if (subScript != script) {
                validateAspects(subScript, state, validationIssues);
            }
        }

        return validationIssues;
    }

    public List<ValidationIssue> validateResources() {
        List<ValidationIssue> validationIssues = new ArrayList<>();
        validateResources(script, resources, validationIssues);
        return validationIssues;
    }

    public void loadScripts() throws ScriptParsingException, ValidationIssue, IOException {
        validateScript(script, new ArrayList<>());
        for (String s : scripts.names()) {
            scripts.get(actor, s);
        }
    }

    public List<ValidationIssue> validateCoverage() throws ScriptParsingException, ValidationIssue, IOException {
        List<ValidationIssue> validationIssues = new ArrayList<>();

        for (String name : scripts.names()) {
            Script s = scripts.get(actor, name);
            for (Action a : retainUncovered(s)) {
                validationIssues.add(new ValidationIssue(s, a, "Action without caller"));
            }
        }

        return validationIssues;
    }

    private List<Action> retainUncovered(Script script) throws ScriptParsingException, ValidationIssue, IOException {
        List<Action> uncovered = script.actions.getAll();

        uncovered.removeAll(script.actions.getAll(script.startRange));
        uncovered.removeAll(script.actions.getAll(script.onAllSet));
        uncovered.removeAll(script.actions.getAll(script.onClose));
        if (script.onRecognitionRejected != null) {
            uncovered.removeAll(script.actions.getAll(script.onRecognitionRejected));
        }

        retainActionsCalledFromOtherActions(script, uncovered);
        retainActionsCalledFromOtherScript(script, uncovered);
        retainDebugEntries(script, uncovered);

        return uncovered;
    }

    private static void retainActionsCalledFromOtherActions(Script script, List<Action> uncovered) {
        for (Action a : script.actions.getAll()) {
            for (ActionRange coverage : a.interaction.coverage()) {
                if (!(coverage instanceof LoadSbdRange)) {
                    uncovered.removeAll(script.actions.getAll(coverage));
                }
            }
        }
    }

    private void retainActionsCalledFromOtherScript(Script script, List<Action> uncovered)
            throws ScriptParsingException, ValidationIssue, IOException {
        for (String callingScriptName : scripts.names()) {
            Script callingScript = scripts.get(actor, callingScriptName);
            if (callingScript != script) {
                for (Action a : callingScript.actions.getAll()) {
                    for (ActionRange coverage : a.interaction.coverage()) {
                        if (coverage instanceof LoadSbdRange) {
                            LoadSbdRange loadSbdRange = (LoadSbdRange) coverage;
                            if (loadSbdRange.script.equalsIgnoreCase(script.name)) {
                                uncovered.removeAll(script.actions.getAll(loadSbdRange));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void retainDebugEntries(Script script, List<Action> uncovered) {
        for (Action a : script.actions.getAll()) {
            if (a.commands != null) {
                for (Command command : a.commands) {
                    if (command instanceof DebugEntry) {
                        uncovered.remove(a);
                    }
                }
            }
        }
    }

    public void reportValidationIssues(List<ValidationIssue> validationIssues) throws ValidationIssue {
        if (!validationIssues.isEmpty()) {
            for (ScriptException validationIssue : validationIssues) {
                if (logger.isInfoEnabled())
                    logger.info(errorMessage(validationIssue));
            }
            throw new ValidationIssue(script, "Validation failed with " + validationIssues.size() + " issues");
        }
    }

    private static void validateAspects(Script script, MappedScriptState state, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        validateScript(script, validationErrors);
        validateMappings(script, state, validationErrors);
    }

    private static void validateMappings(Script script, MappedScriptState state,
            List<ValidationIssue> validationErrors) {
        validateCommands(script, script, script.commands, state, validationErrors);
        for (Action action : script.actions.getAll()) {
            validateCommands(script, action, action.commands, state, validationErrors);
        }
    }

    private static void validateCommands(Script script, AbstractAction abstractAction, List<Command> commands,
            MappedScriptState state, List<ValidationIssue> validationErrors) {
        if (commands != null) {
            for (Command command : commands) {
                if (command instanceof ResetRange) {
                    validateCommand(script, abstractAction, (ActionRange) command, state, validationErrors);
                }
            }
        }
    }

    private static void validateCommand(Script script, AbstractAction abstractAction, ActionRange range,
            MappedScriptState state, List<ValidationIssue> validationErrors) {
        for (int n : range) {
            if (state.hasScriptValueMapping(n)) {
                validationErrors.add(new ValidationIssue(script,
                        abstractAction + ": .resetrange may not unset mapped item or state " + n));
            }
        }
    }

    private void resetScript() throws ScriptExecutionException {
        invokedOnAllSet = false;
        if (script.onClose != null) {
            enableOnClose();
        }
        script.execute(state);
    }

    private void enableOnClose() {
        Thread scriptThread = Thread.currentThread();
        teaseLib.host.setQuitHandler(event -> {
            logger.info("Interrupting script thread '{}'", scriptThread.getName());
            scriptInterruptedReason.set(event.reason());
            scriptThread.interrupt();
            // -> interrupting the main script continues at the onClose range
        });
    }

    /**
     * Plays the given range. Returns on {@code .quit}, if {@link Player#EndAction} had been pushed on the stack and a
     * return statement is executed, or if the the next action is not a member of the play range.
     * 
     * @param playRange
     *            The range to play.
     * @throws AllActionsSetException
     * @throws ScriptExecutionException
     */
    public void playRange(ActionRange playRange) throws ScriptExecutionException {
        ActionRange previous = this.playRange;
        try {
            this.playRange = playRange;
            while (action != EndAction && playRange.contains(action.number)) {
                var breakPoint = breakPoints.getBreakPoint(script.name, action);
                breakPoint.reached();
                if (breakPoint.suspend()) {
                    return;
                }

                try {
                    action = execute(action);
                } catch (ScriptInterruptedException e) {
                    // Because script functions in the break range statement are
                    // running in the same player instance as the main script,
                    // we have to check the play range in order to find out if the
                    // onClose handler should be called.
                    // It's kind of a hack, and leaves a small loop hole
                    // (placing the onClose range inside the play range),
                    // but saves us from creating a second player instance
                    boolean callOnCloseHandler = script.onClose != null //
                            && playRange.contains(script.onClose)//
                            && scriptInterruptedReason.get() != null;
                    if (callOnCloseHandler) {
                        Thread.interrupted();
                        scriptInterruptedReason.set(null);
                        endAll();
                        action = getAction(script.onClose);
                    } else {
                        throw e;
                    }
                } catch (ScriptExecutionException e) {
                    throw e;
                } catch (RuntimeException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ScriptExecutionException) {
                        throw (ScriptExecutionException) cause;
                    } else {
                        throw new ScriptExecutionException(script, action, e);
                    }
                }
            }
        } finally {
            this.playRange = previous;
        }
    }

    public void loadScript(LoadSbdRange range)
            throws ScriptParsingException, ValidationIssue, IOException, ScriptExecutionException {
        setScript(script.load(range.script));
        resetScript();
        // Jumping into a different script
        // definitely exits the play range
        this.action = getAction(range);
    }

    public Action getAction(ActionRange range) throws AllActionsSetException {
        List<Action> actions = range(script, range);
        var candidate = chooseAction(actions);
        if (candidate == null) {
            logger.info("All actions set");
            if (script.onAllSet != null && !invokedOnAllSet) {
                logger.info("Invoking OnAllSet handler");
                invokedOnAllSet = true;
                ActionRange onAllSet = script.onAllSet;
                actions = range(script, onAllSet);
                if (actions.isEmpty()) {
                    throw new AllActionsSetException(script, action, onAllSet);
                } else {
                    candidate = chooseAction(actions);
                }
            } else {
                throw new AllActionsSetException(script, action, range);
            }
        }
        return candidate;
    }

    public Action execute(Action action) throws ScriptExecutionException {
        action.execute(state);
        Action next = action.interaction.getRange(this, script, action, () -> render(action));
        if (Thread.interrupted()) {
            throw new ScriptInterruptedException();
        }

        return next;
    }

    private void render(Action action) {
        if (action.visuals != null) {
            for (Visual visual : action.visuals.values()) {
                render(visual);
            }
        }
        if (action.message != null) {
            render(action.message);
        }
    }

    public void render(Visual visual) {
        visual.render(this);
    }

    /**
     * Build a list of all executable actions, after evaluating conditions.
     * 
     * @param range
     * @return List of available actions.
     */
    public List<Action> range(ActionRange range) {
        return range(script, range);
    }

    /**
     * Build a list of all executable actions, after evaluating conditions.
     * 
     * @param script
     * @param range
     * @return List of available actions.
     */
    private List<Action> range(Script script, ActionRange range) {
        // Get all available, e.g. those not set yet
        List<Action> candidates = script.actions.getUnset(range, state);
        List<Action> selectable = new LinkedList<>();
        List<ConditionRange> relaxedConditions;
        Iterator<ConditionRange> conditionsRanges;
        relaxedConditions = new ArrayList<>(script.conditionRanges.size());
        conditionsRanges = script.conditionRanges.iterator();
        while (true) {
            List<Action> poss0 = null;
            List<Action> poss100 = null;
            for (Action candidate : candidates) {
                boolean getAction = evalConditions(candidate, relaxedConditions);
                if (getAction) {
                    // poss == 1 and poss == 100 are special cases
                    if (candidate.poss != null) {
                        if (candidate.poss == 0) {
                            if (poss0 == null) {
                                poss0 = new LinkedList<>();
                            }
                            poss0.add(candidate);
                        } else if (candidate.poss == 100) {
                            if (poss100 == null) {
                                poss100 = new LinkedList<>();
                            }
                            poss100.add(candidate);
                        } else {
                            selectable.add(candidate);
                        }
                    } else {
                        selectable.add(candidate);
                    }
                }
            }
            if (poss100 != null) {
                // poss == 100 overrides
                logger.info("choosing poss100 - override");
                return poss100;
            } else if (!selectable.isEmpty()) {
                // No poss null -> selectable
                return selectable;
            } else if (poss0 != null) {
                // selectable.size() == 0 but actions in else branch
                logger.info("else");
                return poss0;
            } else if (conditionsRanges.hasNext()) {
                ConditionRange relaxed = conditionsRanges.next();
                if (!relaxed.equals(Script.DefaultConditionRange) && logger.isDebugEnabled()) {
                    logger.debug("Relaxing {}", relaxed);
                }
                relaxedConditions.add(relaxed);
            } else {
                return Collections.emptyList();
            }
        }
    }

    private boolean evalConditions(Action action, List<ConditionRange> conditionRanges) {
        if (action.conditions != null) {
            for (Condition condition : action.conditions) {
                if (!ignoreOptionalCondition(condition, conditionRanges) && !condition.isTrueFor(state)) {
                    if (logger.isInfoEnabled())
                        logger.info("Action " + action.number + ": " + condition.getClass().getSimpleName() + " "
                                + condition.toString());
                    return false;
                }
            }
        }
        return true;
    }

    public boolean ignoreOptionalCondition(Condition condition, List<ConditionRange> conditionRanges) {
        boolean isOptionalCondition = condition instanceof Should;
        if (conditionRanges != null && isOptionalCondition) {
            for (ConditionRange conditionRange : conditionRanges) {
                if (condition.isInside(conditionRange)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Choose an action from the list based on probability weights
     * 
     * @param actions
     *            A list of executable actions, sorted out by getActions(...)
     * @return
     */
    public Action chooseAction(List<Action> actions) {
        if (actions.isEmpty()) {
            return null;
        } else if (actions.size() == 1) {
            Action chosenAction = actions.get(0);
            logger.info("Action {}", chosenAction.number);
            return chosenAction;
        } else {
            return probabilityModel.chooseActionBasedOnPossValue(actions);
        }
    }

    public void render(MediaRenderer mediaRenderer) {
        scriptRenderer.queueRenderer(mediaRenderer);
    }

    public static void validateScript(Script script, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        script.validate(validationErrors);
        for (Action action : script.actions.values()) {
            action.validate(script, validationErrors);
        }
    }

    public static void validateResources(Script script, ResourceLoader resourceLoader,
            List<ValidationIssue> validationIssues) {
        for (Action action : script.actions.values()) {
            testResources(script, action, resourceLoader, validationIssues);
        }
    }

    private static void testResources(Script script, Action action, ResourceLoader resourceLoader,
            List<ValidationIssue> validationIssues) {
        for (String resource : action.validateResources()) {
            InputStream stream = null;
            try {
                stream = resourceLoader.get(resource);
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                validationIssues.add(new ValidationIssue(script, action, e));
            }
        }
    }

    private void reportError(ScriptException e) {
        showError(e, errorMessage(e));
    }

    private void reportError(Throwable t, String scriptName) {
        showError(t, errorMessage(t, scriptName));
    }

    private static String errorMessage(ScriptException e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private static String errorMessage(Throwable t, String scriptName) {
        Throwable cause = t.getCause();
        if (cause != null) {
            if (cause instanceof ScriptException) {
                return errorMessage((ScriptException) cause);
            } else {
                return "Script " + scriptName + ": " + t.getMessage() + "\n" + cause.getClass().getSimpleName() + ": "
                        + cause.getMessage();
            }
        } else {
            return "Script " + scriptName + ", " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
    }

    private void showError(Throwable t, String error) {
        logger.error(t.getMessage(), t);
        logger.error(error);
        try {
            setImage(Message.NoImage);
            show(error);
            reply("Oh Dear");
        } catch (ScriptInterruptedException e) {
            // Ignore
        }
    }

    @SafeVarargs
    public final <T> T randomItem(T... items) {
        return random.item(items);
    }

    public int randomValue(int from, int to) {
        return random.value(from, to);
    }

}
