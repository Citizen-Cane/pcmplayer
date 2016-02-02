/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.Duration;
import pcm.state.State;
import pcm.state.conditions.TimeCondition;
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
public class ScriptTimeTests {

    Player player = new Player(
            TeaseLib.init(new DummyHost(), new DummyPersistence()),
            new ResourceLoader("bin.test/pcm", "test-resources"),
            new Actor(Actor.Dominant, Voice.Gender.Female, "en-us"), "PCM-Test",
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
        return player.range(new ActionRange(n))
                .contains(player.script.actions.get(n));
    }

    @Test
    public void testDuration() throws Exception {
        final long now = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        Date date = new Date(now);
        assertEquals(now, date.getTime());
        assertEquals(0, new Duration("00:00\"00").getTimeSpanMillis());
        assertEquals(+1000, new Duration("+00:00\"01").getTimeSpanMillis());
        assertEquals(-1000, new Duration("-00:00\"01").getTimeSpanMillis());
        assertEquals(+1000 * 30,
                new Duration("+00:00\"30").getTimeSpanMillis());
        assertEquals(-1000 * 30,
                new Duration("-00:00\"30").getTimeSpanMillis());
        assertEquals(+1000 * 60 * 15,
                new Duration("+00:15\"00").getTimeSpanMillis());
        assertEquals(-1000 * 60 * 15,
                new Duration("-00:15\"00").getTimeSpanMillis());
        assertEquals(+1000 * 60 * 30 + 1000 * 60 * 60,
                new Duration("+01:30\"00").getTimeSpanMillis());
        assertEquals(-1000 * 60 * 30 - 1000 * 60 * 60,
                new Duration("-01:30\"00").getTimeSpanMillis());

        assertEquals(0, new Duration("00:00").getTimeSpanMillis());
        assertEquals(+1000 * 60 * 15,
                new Duration("+00:15").getTimeSpanMillis());
        assertEquals(-1000 * 60 * 15,
                new Duration("-00:15").getTimeSpanMillis());
        assertEquals(+1000 * 60 * 30 + 1000 * 60 * 60,
                new Duration("+01:30").getTimeSpanMillis());
        assertEquals(-1000 * 60 * 30 - 1000 * 60 * 60,
                new Duration("-01:30").getTimeSpanMillis());
    }

    @Test
    public void testTimeCondtionToString() throws Exception {
        assertEquals("00:00\"00", TimeCondition.toString(0));
        assertEquals("00:00\"01", TimeCondition.toString(1000));
        assertEquals("00:00\"59", TimeCondition.toString(1000 * 59));
        assertEquals("00:01\"30",
                TimeCondition.toString(1000 * 60 + 1000 * 30));
        assertEquals("00:29\"45",
                TimeCondition.toString(29 * 1000 * 60 + 1000 * 45));
        assertEquals("02:42\"05", TimeCondition
                .toString(2 * 60 * 60 * 1000 + 42 * 1000 * 60 + 1000 * 5));
        assertEquals("13:39\"05", TimeCondition
                .toString(13 * 60 * 60 * 1000 + 39 * 1000 * 60 + 1000 * 5));

        assertEquals("00:01\"00", TimeCondition.toString(1000 * 60));
        assertEquals("00:29\"00", TimeCondition.toString(29 * 1000 * 60));
        assertEquals("02:42\"00",
                TimeCondition.toString(2 * 60 * 60 * 1000 + 42 * 60 * 1000));
        assertEquals("13:39\"00",
                TimeCondition.toString(13 * 60 * 60 * 1000 + 39 * 60 * 1000));
    }

    @Test
    public void testTimeFromTo() throws Exception {
        ActionRange r = new ActionRange(1000);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1000));
        player.teaseLib.sleep(1, TimeUnit.SECONDS);
        assertTrue(containsAction(1001));
        assertTrue(!containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(!containsAction(1004));
    }

    @Test
    public void testTimeFromToOffset() throws Exception {
        ActionRange r = new ActionRange(1010);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1010));
        player.teaseLib.sleep(500, TimeUnit.MILLISECONDS);
        assertTrue(containsAction(1011));
        assertTrue(!containsAction(1012));
        assertTrue(containsAction(1013));
        assertTrue(!containsAction(1014));
    }

}
