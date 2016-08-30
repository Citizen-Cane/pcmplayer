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
import pcm.state.conditions.TimeCondition;
import teaselib.Actor;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.texttospeech.Voice;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

/**
 * @author someone
 *
 */
public class ScriptTimeTests {

    Player player = new Player(
            new TeaseLib(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(ScriptTimeTests.class),
            new Actor("Test", Voice.Gender.Female, "en-us"), "pcm", null) {

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
        player.teaseLib.sleep(2, TimeUnit.SECONDS);
        assertTrue(containsAction(1001));
        assertTrue(!containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(!containsAction(1004));
        assertTrue(!containsAction(1005));
    }

    @Test
    public void testTimeFromToOffset() throws Exception {
        ActionRange r = new ActionRange(1010);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1010));
        player.teaseLib.sleep(2, TimeUnit.SECONDS);
        assertTrue(containsAction(1011));
        assertTrue(!containsAction(1012));
        assertTrue(!containsAction(1013));
        assertTrue(containsAction(1014));

        assertTrue(!containsAction(1015));
        assertTrue(containsAction(1016));
        assertTrue(!containsAction(1017));
    }

    @Test
    public void testInfinityPlus() throws Exception {
        ActionRange r = new ActionRange(1020);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1020));
        assertTrue(!containsAction(1021));
        assertTrue(containsAction(1022));
    }

    @Test
    public void testInfinityMinus() throws Exception {
        ActionRange r = new ActionRange(1025);
        player.range = r;
        player.play(r);
        assertEquals(State.SET, player.state.get(1025));
        assertTrue(containsAction(1026));
        assertTrue(containsAction(1027));
        assertTrue(!containsAction(1028));
        assertTrue(!containsAction(1029));
    }

}
