/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.Duration;
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
public class ScriptTimeTests {

    Player player = new Player(TeaseLib.init(new DummyHost(),
            new DummyPersistence()), new ResourceLoader("bin.test/pcm",
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
        player.loadScript("ScriptTimeTests");
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n)).contains(
                player.script.actions.get(n));
    }

    @Test
    public void testDuration() throws Exception {
        final long now = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        Date date = new Date(now);
        assertEquals(now, date.getTime());
        assertEquals(0, new Duration("00:00\"00").getTime());
        assertEquals(+1000, new Duration("+00:00\"01").getTime());
        assertEquals(-1000, new Duration("-00:00\"01").getTime());
        assertEquals(+1000 * 30, new Duration("+00:00\"30").getTime());
        assertEquals(-1000 * 30, new Duration("-00:00\"30").getTime());
        assertEquals(+1000 * 60 * 15, new Duration("+00:15\"00").getTime());
        assertEquals(-1000 * 60 * 15, new Duration("-00:15\"00").getTime());
        assertEquals(+1000 * 60 * 30 + 1000 * 60 * 60, new Duration(
                "+01:30\"00").getTime());
        assertEquals(-1000 * 60 * 30 - 1000 * 60 * 60, new Duration(
                "-01:30\"00").getTime());
    }

    @Test
    public void testTimeFromTo() throws Exception {
        ActionRange r = new ActionRange(1000, 1001);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1000));
        assertTrue(containsAction(1001));
        assertTrue(!containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(!containsAction(1004));
    }

    @Test
    public void testTimeOffset() throws Exception {
        ActionRange r = new ActionRange(1010, 1011);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1010));
        assertTrue(containsAction(1011));
        assertTrue(!containsAction(1012));
        assertTrue(containsAction(1013));
        assertTrue(!containsAction(1014));
    }

}
