package pcm.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;
import teaselib.Actor;
import teaselib.Sexuality.Gender;
import teaselib.core.Debugger;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.configuration.DebugSetup;
import teaselib.core.debug.DebugHost;
import teaselib.test.TestScript;

/**
 * @author Citizen-Cane
 *
 */
public class TestPlayer extends Player {

    public static final String NAMESPACE = "Test_Namespace";

    public final Debugger debugger;

    public static TeaseLib teaseLib() throws IOException {
        return new TeaseLib(new DebugHost(), new DebugSetup().ignoreMissingResources());
    }

    public TestPlayer(Class<?> scriptClass) throws IOException {
        this(teaseLib(), scriptClass);
    }

    public TestPlayer(Class<?> scriptClass, Actor actor) throws IOException {
        this(new TeaseLib(new DebugHost(), new DebugSetup().ignoreMissingResources()), scriptClass, actor);
    }

    public TestPlayer(TeaseLib teaseLib, Class<?> scriptClass) {
        this(teaseLib, scriptClass, TestScript.newActor(Gender.Feminine));
    }

    public TestPlayer(TeaseLib teaseLib, Class<?> scriptClass, Actor dominant) {
        super(teaseLib, new ResourceLoader(scriptClass), dominant, NAMESPACE, null, scriptClass.getSimpleName());
        debugger = new Debugger(teaseLib);
        debugger.freezeTime();
        teaseLib.globals.store(debugger);
    }

    public static TestPlayer loadScript(Class<?> scriptClass)
            throws IOException, ScriptParsingException, ValidationIssue, ScriptExecutionException {
        return loadScript(scriptClass, scriptClass.getSimpleName());
    }

    public static TestPlayer loadScript(Class<?> scriptClass, String script)
            throws ScriptParsingException, ScriptExecutionException, IOException, ValidationIssue {
        TestPlayer player = new TestPlayer(scriptClass);
        player.loadScript(script);
        return player;
    }

    public void play(int start) throws ScriptExecutionException {
        play(new ActionRange(start), ActionRange.all);
    }

    public void play(ActionRange start) throws ScriptExecutionException {
        play(start, ActionRange.all);
    }

    public static List<Condition> umatchedConditions(Action action, ScriptState state) {
        List<Condition> umatchedConditions = new ArrayList<>();
        for (Condition condition : action.conditions) {
            if (!condition.isTrueFor(state)) {
                umatchedConditions.add(condition);
            }
        }
        return umatchedConditions;
    }

    public static String toString(List<Condition> unmatchedconditions) {
        StringBuilder string = new StringBuilder();
        for (Condition condition : unmatchedconditions) {
            if (string.length() == 0) {
                string.append("[");
            } else {
                string.append(", ");
            }
            string.append(condition.getClass().getSimpleName() + "=" + condition.toString());
        }
        return string.toString();
    }
}
