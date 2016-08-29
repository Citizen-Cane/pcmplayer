/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.State;
import teaselib.Actor;
import teaselib.TeaseLib;
import teaselib.core.ResourceLoader;
import teaselib.core.texttospeech.Voice;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

/**
 * @author someone
 *
 */
public class ScriptTests {

    Player player = new Player(
            new TeaseLib(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(ScriptTests.class),
            new Actor(Actor.Key.DominantFemale, Voice.Gender.Female, "en-us"),
            "pcm", null) {

        @Override
        public void run() {
        }
    };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("ScriptTests");
    }

    @Test
    public void testOutOfActions() throws Exception {
        ActionRange r = new ActionRange(1000, 1001);
        player.range = r;
        player.state.set(9);
        player.play(r);
        assertEquals(State.SET, player.state.get(1000));
        assertEquals(State.UNSET, player.state.get(1001));
        assertEquals(9999, player.range.start);
        try {
            player.range = r;
            player.play(r);
            assertTrue("Unexpected action available", false);
        } catch (AllActionsSetException e) {
            assertEquals(State.SET, player.state.get(9));
            assertEquals(State.SET, player.state.get(1000));
            assertEquals(State.UNSET, player.state.get(1001));
        }
    }

    // Logged repeatAdd/Del, added test case to document the feature
    @Test
    public void testRepeatAdd() throws Exception {
        ActionRange r = new ActionRange(1010, 1011);
        assertEquals(State.UNSET, player.state.get(1010));
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1010));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(-2, player.state.get(1011).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-1, player.state.get(1011).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-0, player.state.get(1011).intValue());
        assertEquals(State.UNSET, player.state.get(1011));
        player.range = r;
        player.play(r);
        assertTrue(player.state.get(1011) == State.SET);
    }

    @Test
    public void testRepeatDel() throws Exception {
        ActionRange r = new ActionRange(1020, 1023);
        assertEquals(State.UNSET, player.state.get(1020));
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1020));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(State.SET, player.state.get(1021));
        assertEquals(State.SET, player.state.get(1022));
        assertEquals(-3, player.state.get(1023).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-1, player.state.get(1023).intValue());
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1023));
    }
}
