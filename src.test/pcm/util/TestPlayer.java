package pcm.util;

import java.io.IOException;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
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

    public static final String TEST_NAMESPACE = "Test_Namespace";

    public final Debugger debugger;

    public TestPlayer(Class<?> scriptClass) throws IOException {
        this(new TeaseLib(new DebugHost(), new DebugSetup()), scriptClass);
    }

    public TestPlayer(TeaseLib teaseLib, Class<?> scriptClass) {
        this(teaseLib, scriptClass, TestScript.newActor(Gender.Feminine));
    }

    public TestPlayer(TeaseLib teaseLib, Class<?> scriptClass, Actor dominant) {
        super(teaseLib, new ResourceLoader(scriptClass), dominant, TEST_NAMESPACE, null, scriptClass.getSimpleName());
        debugger = new Debugger(teaseLib);
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

}
