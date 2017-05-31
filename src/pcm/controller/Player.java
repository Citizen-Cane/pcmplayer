package pcm.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.model.AbstractAction;
import pcm.model.Action;
import pcm.model.ActionLoadSbd;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptException;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import pcm.state.Command;
import pcm.state.Condition;
import pcm.state.Visual;
import pcm.state.commands.ResetRange;
import pcm.state.conditions.Should;
import pcm.state.conditions.ShouldNot;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.ScriptState;
import teaselib.Actor;
import teaselib.Images;
import teaselib.Message;
import teaselib.ScriptFunction;
import teaselib.Sexuality;
import teaselib.Sexuality.Gender;
import teaselib.TeaseScript;
import teaselib.core.ResourceLoader;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.TeaseLib;
import teaselib.core.devices.remote.KeyRelease;
import teaselib.core.media.MediaRenderer;
import teaselib.core.speechrecognition.SpeechRecognitionResult.Confidence;
import teaselib.core.texttospeech.ScriptScanner;
import teaselib.core.texttospeech.TextToSpeechRecorder;
import teaselib.core.texttospeech.Voice;
import teaselib.util.RandomImages;
import teaselib.util.SpeechRecognitionRejectedScript;
import teaselib.util.TextVariables;

public abstract class Player extends TeaseScript {
    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    public static final String ScriptFolder = "scripts/";

    public Script script = null;
    public final MappedScriptState state;
    private final ProbabilityModel probabilityModel = new ProbabilityModelBasedOnPossBucketSum() {
        @Override
        public double random(double from, double to) {
            return Player.this.random(from, to);
        }
    };
    public ActionRange range = null;

    private final ScriptCache scripts;
    private final String mistressPath;
    public final String namespaceApplyAttribute;

    public boolean validateScripts = false;
    public boolean debugOutput = false;
    private boolean invokedOnAllSet = false;
    private boolean intentionalQuit = false;

    // TODO Enable key-release in both main and sub-scripts
    // TODO if started in main already, commands in sub script should be ignored
    // TODO if started in main, the key shouldn't be released in the sub script
    public KeyRelease keyRelease = null;
    public int keyReleaseActuator = -1;

    /**
     * This range can be pushed onto the script range stack to tell the player
     * to hand over execution from the PCM script engine back to the player.
     * 
     * Push it on the stack, and call {@link Player#play(ActionRange)}. The
     * current script is executed up to the next occurrence of a
     * {@link AbstractAction.Statement#Return} statement.
     */
    public final static ActionRange ReturnToPlayer = new ActionRange(0);

    public static void recordVoices(Actor actor, String mainScript, File path, String resourcesRoot, String[] assets)
            throws IOException, ValidationIssue, ScriptParsingException {
        ResourceLoader resources = new ResourceLoader(path, resourcesRoot);
        resources.addAssets(assets);
        TextToSpeechRecorder recorder = new TextToSpeechRecorder(path, resourcesRoot, resources, new TextVariables());
        Symbols dominantSubmissiveRelations = Symbols.getDominantSubmissiveRelations();
        for (Entry<String, String> entry : dominantSubmissiveRelations.entrySet()) {
            Symbols dominantSubmissiveRelation = new Symbols();
            dominantSubmissiveRelation.put(entry.getKey(), entry.getValue());
            ScriptCache scripts = new ScriptCache(resources, Player.ScriptFolder, dominantSubmissiveRelation);
            // Get the main script
            Script main = scripts.get(actor, mainScript);
            // and validate to load all the sub scripts
            // TODO load scripts explicitly
            validateScript(main, new ArrayList<ValidationIssue>());
            recorder.preparePass(entry);
            for (String scriptName : scripts.names()) {
                Script script = scripts.get(actor, scriptName);
                ScriptScanner scriptScanner = new PCMScriptScanner(script);
                recorder.create(scriptScanner);
            }
        }
        recorder.finish();
    }

    public Player(TeaseLib teaseLib, ResourceLoader resources, Actor actor, String namespace, String mistressPath) {
        super(teaseLib, resources, actor, namespace);
        this.scripts = new ScriptCache(resources, Player.ScriptFolder, createDominantSubmissiveSymbols());
        this.mistressPath = mistressPath;
        this.namespaceApplyAttribute = "Applied.by." + namespace;
        this.invokedOnAllSet = false;
        this.state = new MappedScriptState(this);
    }

    private Symbols createDominantSubmissiveSymbols() {
        StringBuilder dominantSubmissiveSymbol = new StringBuilder();
        if (actor.gender == Voice.Gender.Male) {
            dominantSubmissiveSymbol.append("M");
        } else {
            dominantSubmissiveSymbol.append("F");
        }
        if (persistentEnum(Sexuality.Sex.class).value() == Sexuality.Sex.Female) {
            dominantSubmissiveSymbol.append("f");
        } else {
            if (persistentEnum(Sexuality.Gender.class).value() == Gender.Feminine) {
                dominantSubmissiveSymbol.append("tv");
            } else {
                dominantSubmissiveSymbol.append("m");
            }
        }
        Symbols staticSymbols = new Symbols();
        staticSymbols.put(dominantSubmissiveSymbol.toString(), "true");
        return staticSymbols;

    }

    public void play(String script) {
        StringTokenizer argv = parseDebugFile(script);
        String scriptName = argv.nextToken();
        ActionRange range = parseStartRange(argv);
        play(scriptName, range);
    }

    private StringTokenizer parseDebugFile(String script) {
        String resourcePath = getClass().getSimpleName() + "/" + "debug.txt";
        try {
            InputStream debugStream = resources.getResource(resourcePath);
            if (debugStream != null) {
                debugOutput = true;
                validateScripts = true;
                BufferedReader debugReader = new BufferedReader(new InputStreamReader(debugStream));
                try {
                    String line;
                    while ((line = debugReader.readLine()) != null) {
                        line = line.trim();
                        int comment = line.indexOf("//");
                        if (comment == 0) {
                            continue;
                        } else if (comment > 0) {
                            line = line.substring(0, comment - 1);
                        }
                        if (!line.isEmpty()) {
                            script = line;
                            break;
                        }
                    }
                } finally {
                    debugReader.close();
                }
            }
        } catch (Exception e) {
            logger.error(resourcePath, e);
        }
        StringTokenizer argv = new StringTokenizer(script, " \t");
        return argv;
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
        SpeechRecognitionRejectedScript srRejectedHandler = actor.speechRecognitionRejectedScript;
        actor.speechRecognitionRejectedScript = new SpeechRecognitionRejectedScript(this) {
            @Override
            public boolean canRun() {
                return script != null && script.onRecognitionRejected != null && !intentionalQuit;
            }

            @Override
            public void run() {
                try {
                    // The play(Range) method must end on return, not continue
                    // somewhere else
                    Player.this.scripts.stack.push(ReturnToPlayer);
                    range = Player.this.script.onRecognitionRejected;
                    Player.this.play(new ActionRange(0, Integer.MAX_VALUE));
                    if (range == null) {
                        // Intentional quit
                        intentionalQuit = true;
                        throw new ScriptInterruptedException();
                        // TODO Interrupting the main script results in the
                        // onClose handler to be triggered -> bad quit
                    }
                } catch (AllActionsSetException e) {
                    logger.error(e.getMessage(), e);
                } catch (ScriptExecutionException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };
        try {
            loadScript(name);
        } catch (ScriptException e) {
            reportError(e);
            return;
        } catch (Throwable t) {
            reportError(t, name);
            return;
        }
        if (script != null) {
            try {
                if (startRange != null) {
                    range = startRange;
                } else {
                    range = script.startRange;
                }
                while (range != null) {
                    play((ActionRange) null);
                }
            } catch (ScriptInterruptedException e) {
                if (!intentionalQuit) {
                    logger.error(e.getMessage(), e);
                }
            } catch (ScriptException e) {
                reportError(e);
            } catch (Throwable e) {
                reportError(e, name);
            } finally {
                teaseLib.host.setQuitHandler(null);
                intentionalQuit = false;
                actor.speechRecognitionRejectedScript = srRejectedHandler;
            }
        }
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
        logger.info("Loading script " + newScript.name);
        script = newScript;
        state.setScript(script);
    }

    private void validateProject() throws ScriptParsingException, ValidationIssue, IOException {
        List<ValidationIssue> validationErrors = new ArrayList<ValidationIssue>();
        validateAspects(script, state, resources, validationErrors);
        // TODO Loaded scripts explicitly - currently they're loaded by loading
        // the main script
        for (String s : scripts.names()) {
            Script subScript = scripts.get(actor, s);
            if (subScript != script) {
                validateAspects(subScript, state, resources, validationErrors);
            }
        }
        if (validationErrors.size() > 0) {
            for (ScriptException scriptError : validationErrors) {
                logger.info(errorMessage(scriptError));
            }
            throw new ValidationIssue("Validation failed, " + validationErrors.size() + " issues", script);
        }
    }

    private static void validateAspects(Script script, MappedScriptState state, ResourceLoader resources,
            List<ValidationIssue> validationErrors) throws ScriptParsingException {
        validateScript(script, validationErrors);
        validateMappings(script, state, validationErrors);
        validateResources(script, resources, validationErrors);
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
                validationErrors.add(new ValidationIssue(
                        abstractAction.toString() + ": .resetrange may not unset mapped item or state " + n, script));
            }
        }
    }

    private void resetScript() throws ScriptExecutionException {
        invokedOnAllSet = false;
        if (script.onClose != null) {
            final Thread scriptThread = Thread.currentThread();
            teaseLib.host.setQuitHandler(new TeaseScript(this) {
                @Override
                public void run() {
                    logger.info("Interrupting script thread '" + scriptThread.getName() + "'");
                    scriptThread.interrupt();
                    // The main script continues at the onClose range
                }
            });
        }
        script.execute(state);
        boolean haveImages = mistressPath != null && script.mistressImages != null;
        actor.images = haveImages ? new RandomImages(resources(mistressPath + script.mistressImages)) : Images.None;
    }

    /**
     * Plays the given range. Returns on {@code .quit}, if
     * {@link Player#ReturnToPlayer} had been pushed on the stack and a return
     * statement is executed, or if the the next action is not a member of the
     * play range.
     * 
     * @param playRange
     *            The range to play.
     * @throws AllActionsSetException
     * @throws ScriptExecutionException
     */
    public void play(ActionRange playRange) throws AllActionsSetException, ScriptExecutionException {
        while (true) {
            // Choose action
            Action action = getAction();
            try {
                if (playRange != null) {
                    if (!playRange.contains(action.number)) {
                        range = new ActionRange(action.number);
                        return;
                    }
                }
                range = execute(action);
                if (range == null) {
                    // Quit
                    setImage(Message.NoImage);
                    show("");
                    break;
                } else if (range == ReturnToPlayer) {
                    // do nothing and temporarily suspends playing
                    // in order to return command to the player
                    // Used to execute a sub-script from java
                    break;
                } else if (range instanceof ActionLoadSbd) {
                    ActionLoadSbd loadSbd = (ActionLoadSbd) range;
                    setScript(loadSbd.script);
                    resetScript();
                    range = loadSbd;
                    // Jumping into a different script
                    // definitely exits the play range
                    action = getAction();
                }
                if (Thread.interrupted()) {
                    throw new ScriptInterruptedException();
                }
            } catch (ScriptInterruptedException e) {
                // Because script functions in the break range statement are
                // running in the same player instance as the main script,
                // we have to check the play range in order to find out if the
                // onClose handler should be called.
                // It's kind of a hack, and leaves a small loop hole
                // (placing the onClose range inside the play range),
                // but saves us from creating a second player instance
                boolean callOnClose = playRange == null
                        || (playRange.contains(script.onClose.start) && playRange.contains(script.onClose.end));
                if (script.onClose != null && callOnClose && !intentionalQuit) {
                    // Done automatically in reply(), otherwise we have to do it
                    endAll();
                    range = script.onClose;
                    // clear interrupted state
                    Thread.interrupted();
                    // Continue with onClose range
                } else {
                    throw e;
                }
            } catch (ScriptExecutionException e) {
                throw e;
            } catch (Throwable t) {
                throw new ScriptExecutionException(action, "Error executing script", t, script);
            }
        }
    }

    private Action getAction() throws AllActionsSetException {
        Action action;
        List<Action> actions = range(script, range);
        action = chooseAction(actions);
        if (action == null) {
            logger.info("All actions set");
            if (script.onAllSet != null && invokedOnAllSet == false) {
                logger.info("Invoking OnAllSet handler");
                invokedOnAllSet = true;
                range = script.onAllSet;
                actions = range(script, range);
                if (actions.size() == 0) {
                    throw new AllActionsSetException(script.actions.getAll(range),
                            new ActionRange(range.start, range.end), script);
                } else {
                    action = chooseAction(actions);
                }
            } else {
                throw new AllActionsSetException(script.actions.getAll(range), new ActionRange(range.start, range.end),
                        script);
            }
        }
        return action;
    }

    public ActionRange execute(final Action action) throws ScriptExecutionException {
        // Mark this action as executed
        state.set(action);
        // Perform commands
        action.execute(state);
        // Render visuals
        // It looks so much better with 1.8 ...
        /*
         * Runnable visuals = () -> { if (action.visuals != null) { for (Visual
         * visual : action.visuals.values()) { visual.render(this); } } };
         */

        // One would think that we have to wait for all visuals to
        // at least complete their mandatory part.
        // But interactions perform different, for instance
        // Ask displays its user interface while the visuals render,
        // to allow the message to be spoken during checkbox selection.
        // Interactions that eventually call choose(...) do this
        // implicitly, but all other classes like Range have to call it
        // when suitable, to prevent text and messages appearing too fast
        Runnable visuals = new Runnable() {
            @Override
            public void run() {
                if (action.visuals != null) {
                    for (Visual visual : action.visuals.values()) {
                        render(visual);
                    }
                }
            }
        };
        return action.interaction.getRange(script, action, visuals, this);
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
    public List<Action> range(Script script, ActionRange range) {
        // Get all available, e.g. those not set yet
        List<Action> candidates = script.actions.getUnset(range, state);
        List<Action> selectable = new LinkedList<Action>();
        List<ActionRange> relaxedConditions;
        Iterator<ActionRange> conditionsRanges;
        relaxedConditions = new ArrayList<ActionRange>(script.conditionRanges.size());
        conditionsRanges = script.conditionRanges.iterator();
        while (true) {
            List<Action> poss0 = null;
            List<Action> poss100 = null;
            for (Action action : candidates) {
                boolean getAction = evalConditions(action, relaxedConditions);
                if (getAction) {
                    // poss == 1 and poss == 100 are special cases
                    if (action.poss != null) {
                        if (action.poss == 0) {
                            if (poss0 == null) {
                                poss0 = new LinkedList<Action>();
                            }
                            poss0.add(action);
                        } else if (action.poss == 100) {
                            if (poss100 == null) {
                                poss100 = new LinkedList<Action>();
                            }
                            poss100.add(action);
                        } else {
                            selectable.add(action);
                        }
                    } else {
                        selectable.add(action);
                    }
                }
            }
            if (poss100 != null) {
                // poss == 100 overrides
                logger.info("choosing poss100 - override");
                return poss100;
            } else if (selectable.size() > 0) {
                // No poss null -> selectable
                return selectable;
            } else if (poss0 != null) {
                // selectable.size() == 0 but actions in else branch
                logger.info("else");
                return poss0;
            } else if (conditionsRanges.hasNext()) {
                ActionRange ignore = conditionsRanges.next();
                if (ignore != null) {
                    if (ignore.start > Integer.MIN_VALUE || ignore.end < Integer.MAX_VALUE) {
                        logger.info("Should/ShouldNot: ignoring " + ignore.toString());
                    }
                }
                relaxedConditions.add(ignore);
                continue;
            } else {
                return Collections.emptyList();
            }
        }
    }

    private boolean evalConditions(Action action, List<ActionRange> conditionRanges) {
        if (action.conditions != null) {
            for (Condition condition : action.conditions) {
                if (ignoreOptionalCondition(condition, conditionRanges)) {
                    continue;
                } else {
                    if (condition.isTrueFor(state)) {
                        continue;
                    } else {
                        logger.info("Action " + action.number + ": " + condition.getClass().getSimpleName() + " "
                                + condition.toString());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean ignoreOptionalCondition(Condition condition, List<ActionRange> conditionRanges) {
        boolean isOptionalCondition = condition instanceof Should || condition instanceof ShouldNot;
        if (conditionRanges != null && isOptionalCondition) {
            @SuppressWarnings("unchecked")
            Collection<Integer> col = (Collection<Integer>) condition;
            for (ActionRange actionRange : conditionRanges) {
                for (int n : col) {
                    if (actionRange.contains(n)) {
                        return true;
                    }
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
        if (actions.size() == 0) {
            return null;
        } else if (actions.size() == 1) {
            Action action = actions.get(0);
            logger.info("Action " + action.number);
            return action;
        } else {
            return probabilityModel.chooseActionBasedOnPossValue(actions);
        }
    }

    public void render(MediaRenderer mediaRenderer) {
        queueRenderer(mediaRenderer);
    }

    @Override
    protected String showChoices(ScriptFunction scriptFunction, Confidence recognitionConfidence,
            List<String> choices) {
        // Display text according to slave's level of articulateness
        Long gag = script != null ? state.get(script.gag) : ScriptState.UNSET;
        if (gag.equals(ScriptState.SET)) {
            // Slave is gagged
            final List<String> processedChoices = new ArrayList<String>(choices.size());
            for (String choice : choices) {
                // The simple solution: replace vocals with consonants
                choice = choice.replace("a", "m");
                choice = choice.replace("e", "m");
                choice = choice.replace("i", "m");
                choice = choice.replace("o", "m");
                choice = choice.replace("u", "m");
                processedChoices.add(choice);
            }
            final String processedChoice = super.showChoices(scriptFunction, recognitionConfidence, processedChoices);
            // Return the original choice instance
            int index = processedChoices.indexOf(processedChoice);
            if (index < 0) {
                // Timeout
                return processedChoice;
            } else {
                return choices.get(index);
            }
        } else {
            return super.showChoices(scriptFunction, recognitionConfidence, choices);
        }
    }

    public static void validateScript(Script script, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        script.validate(validationErrors);
        for (Action action : script.actions.values()) {
            action.validate(script, validationErrors);
        }
        for (ScriptException scriptError : validationErrors) {
            if (scriptError.script == null) {
                scriptError.script = script;
            }
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
                stream = resourceLoader.getResource(resource);
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                validationIssues.add(new ValidationIssue(action, e, script));
            }
        }
    }

    private void reportError(ScriptException e) {
        showError(e, errorMessage(e));
    }

    private void reportError(Throwable t, String scriptName) {
        showError(t, errorMessage(t, scriptName));
    }

    private String errorMessage(ScriptException e) {
        String scriptName = e.script != null ? e.script.name : script.name;
        return errorMessage(e, scriptName);
    }

    private static String errorMessage(Throwable t, String scriptName) {
        Throwable cause = t.getCause();
        if (cause != null) {
            return "Script " + scriptName + ": " + t.getMessage() + "\n" + cause.getClass().getSimpleName() + ": "
                    + cause.getMessage();
        } else {
            return "Script " + scriptName + ", " + t.getClass().getName() + ": " + t.getMessage();
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
}
