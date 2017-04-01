/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.persistence.ScriptState;
import teaselib.Actor;
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
public class ScriptTimeMineExampleTest {

    private final Player player = new Player(
            new TeaseLib(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(ScriptTimeMineExampleTest.class),
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
        player.loadScript("ScriptTimeMineExampleTest");
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n))
                .contains(player.script.actions.get(n));
    }

    @Test
    public void testThatMineScriptExampleTablesAreCorrect() throws Exception {
        debugger.freezeTime();

        ActionRange r = new ActionRange(1000);
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1000));

        assertFalse(containsAction(1001));
        assertTrue(containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertFalse(containsAction(1007));
        assertFalse(containsAction(1008));
        assertFalse(containsAction(1009));
        assertFalse(containsAction(1010));

        debugger.advanceTime(10, TimeUnit.MINUTES);

        assertFalse(containsAction(1001));
        assertFalse(containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertTrue(containsAction(1007));
        assertTrue(containsAction(1008));
        assertFalse(containsAction(1009));
        assertFalse(containsAction(1010));

        debugger.advanceTime(10, TimeUnit.MINUTES);

        assertFalse(containsAction(1001));
        assertFalse(containsAction(1002));
        assertFalse(containsAction(1003));
        assertFalse(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertTrue(containsAction(1007));
        assertTrue(containsAction(1008));
        assertTrue(containsAction(1009));
        assertTrue(containsAction(1010));
    }
}
