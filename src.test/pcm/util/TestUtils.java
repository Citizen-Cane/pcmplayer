package pcm.util;

import java.util.Locale;

import pcm.ConditionalParsingTest;
import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import teaselib.Actor;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.texttospeech.Voice;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

public class TestUtils {

    public static Player createPlayer() {
        Player player = new Player(
                new TeaseLib(new DummyHost(), new DummyPersistence()),
                new ResourceLoader(ConditionalParsingTest.class,
                        ResourceLoader.ResourcesInProjectFolder),
                new Actor(Actor.Key.DominantFemale, Voice.Gender.Female,
                        Locale.US),
                "pcm", null) {

            @Override
            public void run() {
            }
        };
        return player;
    }

    public static void play(Player player, ActionRange start,
            ActionRange playRange)
            throws AllActionsSetException, ScriptExecutionException {
        player.range = start;
        player.play(playRange);
    }

}
