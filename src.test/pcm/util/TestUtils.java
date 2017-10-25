package pcm.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Condition;
import pcm.state.persistence.ScriptState;
import teaselib.Actor;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.debug.DebugHost;
import teaselib.core.debug.DebugPersistence;
import teaselib.core.texttospeech.Voice.Gender;
import teaselib.test.DebugSetup;

public class TestUtils {
    public static final String TEST_NAMESPACE = "Test_Namespace";

    public static TeaseLib teaseLib() throws IOException {
        TeaseLib teaseLib = new TeaseLib(new DebugHost(), new DebugPersistence(), new DebugSetup());
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
