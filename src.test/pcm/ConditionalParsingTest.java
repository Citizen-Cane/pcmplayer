/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.State;
import pcm.util.TestUtils;

/**
 * @author someone
 *
 */
public class ConditionalParsingTest {
    Player player;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player = TestUtils.createPlayer();
        player.loadScript("ConditionalParsingTest");
        assertEquals(3, player.script.actions.size());
    }

    @Test
    public void testPath_Else_in_If_Else() throws Exception {
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);
        assertEquals(State.SET, player.state.get(r.start));
        assertEquals("#else not parsed -", State.SET, player.state.get(3));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testPath_Else_in_If_If_Else() throws Exception {
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);
        assertEquals(State.SET, player.state.get(r.start));
        assertEquals("#else not parsed -", State.SET, player.state.get(3));
        assertEquals(9999, player.range.start);
    }
}
