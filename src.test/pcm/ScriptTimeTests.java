/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.DurationFormat;
import pcm.model.ScriptExecutionException;
import pcm.state.conditions.TimeTo;
import pcm.state.persistence.ScriptState;
import teaselib.Actor;
import teaselib.State;
import teaselib.core.Debugger;
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

    private final Player player = new Player(
            new TeaseLib(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(ScriptTimeTests.class),
            new Actor("Test", Voice.Gender.Female, Locale.US), "pcm", null) {

        @Override
        public void run() {
        }
    };

    private final Debugger debugger = new Debugger(player.teaseLib);

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        debugger.freezeTime();
        player.loadScript("ScriptTimeTests");
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n))
                .contains(player.script.actions.get(n));
    }

    @Test
    public void testDuration() throws Exception {
        long now = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        Date date = new Date(now);
        assertEquals(now, date.getTime());
        assertEquals(0, new DurationFormat("00:00\"00").toSeconds());
        assertEquals(+1, new DurationFormat("+00:00\"01").toSeconds());
        assertEquals(-1, new DurationFormat("-00:00\"01").toSeconds());
        assertEquals(+1 * 30, new DurationFormat("+00:00\"30").toSeconds());
        assertEquals(-1 * 30, new DurationFormat("-00:00\"30").toSeconds());
        assertEquals(+1 * 60 * 15,
                new DurationFormat("+00:15\"00").toSeconds());
        assertEquals(-1 * 60 * 15,
                new DurationFormat("-00:15\"00").toSeconds());
        assertEquals(+1 * 60 * 30 + 1 * 60 * 60,
                new DurationFormat("+01:30\"00").toSeconds());
        assertEquals(-1 * 60 * 30 - 1 * 60 * 60,
                new DurationFormat("-01:30\"00").toSeconds());

        assertEquals(0, new DurationFormat("00:00").toSeconds());
        assertEquals(60 * 15, new DurationFormat("+00:15").toSeconds());
        assertEquals(-60 * 15, new DurationFormat("-00:15").toSeconds());
        assertEquals(+60 * 30 + 60 * 60,
                new DurationFormat("+01:30").toSeconds());
        assertEquals(-60 * 30 - 60 * 60,
                new DurationFormat("-01:30").toSeconds());
    }

    @Test
    public void testTimeFromTo() throws Exception {
        ActionRange r = new ActionRange(1000);
        player.range = r;
        player.play(r);

        assertEquals(ScriptState.SET, player.state.get(1000));

        debugger.advanceTime(2, TimeUnit.SECONDS);

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
        assertEquals(ScriptState.SET, player.state.get(1010));

        debugger.advanceTime(2, TimeUnit.SECONDS);

        assertTrue(containsAction(1011));
        assertFalse(containsAction(1012));
        assertFalse(containsAction(1013));
        assertTrue(containsAction(1014));

        assertFalse(containsAction(1015));
        assertTrue(containsAction(1016));
        assertFalse(containsAction(1017));
    }

    @Test
    public void testInfinityPlus() throws Exception {
        ActionRange r = new ActionRange(1020);
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1020));
        assertFalse(containsAction(1021));
        assertTrue(containsAction(1022));
        assertTrue(containsAction(1023));
    }

    @Test(expected = ScriptExecutionException.class)
    public void testThatInfinityMinusIsAnIllegalArgumentToSetTime()
            throws AllActionsSetException, ScriptExecutionException {
        ActionRange r = new ActionRange(1025);
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1025));
        assertTrue(containsAction(1026));
        assertTrue(containsAction(1027));
        assertFalse(containsAction(1028));
        assertFalse(containsAction(1029));
    }

    @Test
    public void showDifferencesOfFiniteVsInfiniteSetTime() throws Exception {
        debugger.freezeTime();

        ActionRange r = new ActionRange(1030);
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1030));

        assertFalse(containsAction(1031));
        assertFalse(containsAction(1032));
        assertTrue(containsAction(1033));

        assertTrue(containsAction(1034));
        debugger.advanceTime(71, TimeUnit.MINUTES);
        assertFalse(containsAction(1034));

        assertTrue(containsAction(1035));

        assertTrue(containsAction(1036));
        assertTrue(containsAction(1037));
        assertTrue(containsAction(1038));
    }

    @Test
    public void showHowToCheckForInfinityInCode() throws Exception {
        debugger.freezeTime();

        player.state.setTime(9, player.duration(10, TimeUnit.SECONDS));
        assertFalse(new TimeTo(9, "-INF").isTrueFor(player.state));

        player.state.setTime(9,
                player.duration(State.INDEFINITELY, TimeUnit.SECONDS));
        assertTrue(new TimeTo(9, "-INF").isTrueFor(player.state));
    }

    @Test
    public void showHowToCheckForInfinityInScript() throws Exception {
        debugger.freezeTime();

        ActionRange r = new ActionRange(1040);
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1040));

        int setTImeFinite = 1041;
        assertFalse(containsAction(setTImeFinite));
        int setTImeInfinite = 1042;
        assertTrue(containsAction(setTImeInfinite));
    }
}
