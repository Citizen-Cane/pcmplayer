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
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

/**
 * @author someone
 *
 */
public class ScriptTests {

    Player player = new Player(TeaseLib.init(new DummyHost(),
            new DummyPersistence()), new ResourceLoader("bin/pcm",
            "test-resources"), new Actor(Actor.Dominant, "en-us"), "PCM-Test",
            null) {

        @Override
        public void run() {
        }
    };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("test");
    }

    @Test
    public void testOutOfActions() throws Exception {
        ActionRange r = new ActionRange(1000, 1001);
        player.range = r;
        player.state.set(9);
        player.play(r);
        assertTrue(player.state.get(1000) == State.SET);
        assertTrue(player.state.get(1001) == State.UNSET);
        assertEquals(9999, player.range.start);
        try {
            player.range = r;
            player.play(r);
            assertTrue("Unexpected action available", false);
        } catch (AllActionsSetException e) {
            assertTrue(player.state.get(9) == State.SET);
            assertTrue(player.state.get(1000) == State.SET);
            assertTrue(player.state.get(1001) == State.UNSET);
        }
    }
}
