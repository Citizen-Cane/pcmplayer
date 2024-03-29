/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.conditions.TimeTo;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;
import teaselib.Duration;
import teaselib.util.DurationFormat;

/**
 * @author Citizen-Cane
 *
 */
public class ScriptTimeTests {
    private final TestPlayer player;

    public ScriptTimeTests() throws IOException, ScriptParsingException, ScriptExecutionException, ValidationIssue {
        this.player = TestPlayer.loadScript(getClass());
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n)).contains(player.script.actions.get(n));
    }

    @Test
    public void testDurationShortFormat() {
        assertEquals(0, new DurationFormat("00:0").toSeconds());
        assertEquals(0, new DurationFormat("0:00").toSeconds());
        assertEquals(0, new DurationFormat("0:0").toSeconds());
        assertEquals(60, new DurationFormat("00:1").toSeconds());
        assertEquals(60, new DurationFormat("00:01").toSeconds());
        assertEquals(3600 + 60, new DurationFormat("1:01").toSeconds());
        assertEquals(3600 + 60, new DurationFormat("1:1").toSeconds());
    }

    @Test
    public void testDuration() throws Exception {
        long now = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        Date date = new Date(now);
        assertEquals(now, date.getTime());
        assertEquals(0, new DurationFormat("00:00\"00").toSeconds());
        assertEquals(0, new DurationFormat("0:0\"0").toSeconds());
        assertEquals(+1, new DurationFormat("+00:00\"01").toSeconds());
        assertEquals(-1, new DurationFormat("-00:00\"01").toSeconds());
        assertEquals(+1 * 30, new DurationFormat("+00:00\"30").toSeconds());
        assertEquals(-1 * 30, new DurationFormat("-00:00\"30").toSeconds());
        assertEquals(+1 * 60 * 15, new DurationFormat("+00:15\"00").toSeconds());
        assertEquals(-1 * 60 * 15, new DurationFormat("-00:15\"00").toSeconds());
        assertEquals(+1 * 60 * 30 + 1 * 60 * 60, new DurationFormat("+01:30\"00").toSeconds());
        assertEquals(-1 * 60 * 30 - 1 * 60 * 60, new DurationFormat("-01:30\"00").toSeconds());

        assertEquals(0, new DurationFormat("00:00").toSeconds());
        assertEquals(60 * 15, new DurationFormat("+00:15").toSeconds());
        assertEquals(-60 * 15, new DurationFormat("-00:15").toSeconds());
        assertEquals(+60 * 30 + 60 * 60, new DurationFormat("+01:30").toSeconds());
        assertEquals(-60 * 30 - 60 * 60, new DurationFormat("-01:30").toSeconds());
    }

    @Test
    public void testTimeFromTo() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1000);
        player.action = player.getAction(r);
        player.playFrom(r);

        assertEquals(ScriptState.SET, player.state.get(1000));

        player.debugger.advanceTime(2, TimeUnit.SECONDS);

        assertTrue(containsAction(1001));
        assertTrue(!containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(!containsAction(1004));
        assertTrue(!containsAction(1005));
    }

    @Test
    public void testTimeFromToOffset() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1010);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1010));

        player.debugger.advanceTime(2, TimeUnit.SECONDS);

        assertTrue(containsAction(1011));
        assertFalse(containsAction(1012));
        assertFalse(containsAction(1013));
        assertTrue(containsAction(1014));

        assertFalse(containsAction(1015));
        assertTrue(containsAction(1016));
        assertFalse(containsAction(1017));
    }

    @Test
    public void testInfinityPlus() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1020);
        player.action = player.getAction(r);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1020));
        assertFalse(containsAction(1021));
        assertTrue(containsAction(1022));
        assertTrue(containsAction(1023));
    }

    @Test(expected = ScriptExecutionException.class)
    public void testThatInfinityMinusIsAnIllegalArgumentToSetTime() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1025);
        player.action = player.getAction(r);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1025));
        assertTrue(containsAction(1026));
        assertTrue(containsAction(1027));
        assertFalse(containsAction(1028));
        assertFalse(containsAction(1029));
    }

    @Test
    public void showDifferencesOfFiniteVsInfiniteSetTime() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1030);
        player.action = player.getAction(r);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1030));

        assertFalse(containsAction(1031));
        assertFalse(containsAction(1032));
        assertTrue(containsAction(1033));

        assertTrue(containsAction(1034));
        player.debugger.advanceTime(71, TimeUnit.MINUTES);
        assertFalse(containsAction(1034));

        assertTrue(containsAction(1035));

        assertTrue(containsAction(1036));
        assertTrue(containsAction(1037));
        assertTrue(containsAction(1038));
    }

    @Test
    public void showHowToCheckForInfinityInCode() {
        player.state.setTime(9, player.duration(10, TimeUnit.SECONDS));
        assertFalse(new TimeTo(9, "-INF").isTrueFor(player.state));

        player.state.setTime(9, player.duration(Duration.INFINITE, TimeUnit.SECONDS));
        assertTrue(new TimeTo(9, "-INF").isTrueFor(player.state));
    }

    @Test
    public void showHowToCheckForInfinityInScript() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1040);
        player.action = player.getAction(r);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1040));

        int setTimeFinite = 1041;
        assertFalse(containsAction(setTimeFinite));
        int setTimeInfinite = 1042;
        assertTrue(containsAction(setTimeInfinite));
    }
}
