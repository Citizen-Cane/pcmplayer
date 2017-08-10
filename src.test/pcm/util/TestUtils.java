package pcm.util;

import java.io.IOException;
import java.util.Locale;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import teaselib.Actor;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.texttospeech.Voice.Gender;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;
import teaselib.test.DebugSetup;

public class TestUtils {
    public static final String TEST_NAMESPACE = "Test_Namespace";

    public static TeaseLib teaseLib() throws IOException {
        TeaseLib teaseLib = new TeaseLib(new DummyHost(), new DummyPersistence(), new DebugSetup());
        return teaseLib;
    }

    public static Player createPlayer(Class<?> scriptClass) throws IOException {
        TeaseLib teaseLib = teaseLib();
        return createPlayer(teaseLib, scriptClass);
    }

    public static Player createPlayer(TeaseLib teaseLib, Class<?> scriptClass) {
        return createPlayer(teaseLib, scriptClass, teaseLib.getDominant(Gender.Female, Locale.US));
    }

    public static Player createPlayer(TeaseLib teaseLib, Class<?> scriptClass, Actor dominant) {
        Player player = new Player(teaseLib, new ResourceLoader(scriptClass), dominant, TEST_NAMESPACE, null) {
            @Override
            public void run() {
            }
        };
        return player;
    }

    public static Player createPlayer(Class<?> scriptClass, String script)
            throws ScriptParsingException, ScriptExecutionException, IOException, ValidationIssue {
        TeaseLib teaseLib = teaseLib();
        Player player = createPlayer(teaseLib, scriptClass);
        player.loadScript(script);
        return player;
    }

    public static void play(Player player, int start) throws AllActionsSetException, ScriptExecutionException {
        play(player, new ActionRange(start), null);
    }

    public static void play(Player player, ActionRange start) throws AllActionsSetException, ScriptExecutionException {
        play(player, start, null);
    }

    public static void play(Player player, ActionRange start, ActionRange playRange)
            throws AllActionsSetException, ScriptExecutionException {
        player.play(start, playRange);
    }
}
