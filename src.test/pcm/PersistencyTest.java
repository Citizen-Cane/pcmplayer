package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.persistence.MappedScriptItemValue;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.Toys;

public class PersistencyTest {

    @Test
    // TODO Throws AllActionsSet - expected test result
    public void testThatResetRangeUnsetsMappings() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptItemValue<Toys>(365, player.items(Toys.Collars)));
        player.item(Toys.Collar).setAvailable(true);

        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(ScriptState.UNSET, player.state.get(365));

        player.validateScripts = false;
        player.loadScript(
                "PersistencyTest_ResetRangeUnsetsMappings_MainScript");

        // Correct because the optimized .resetrange only clears previously
        // restored items,
        // and thus won't clear the toy anymore
        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(ScriptState.SET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        try {
            // This throws since when returning from the subscript,
            // action 365 has been set, and is thus cleared by .resetrange
            pcm.util.TestUtils.play(player, new ActionRange(1000), null);
            assertTrue(false);
        } catch (AllActionsSetException e) {
            assertEquals(1, e.actions.size());
            assertEquals(1001, e.actions.get(0).number);
        }
    }

    @Test
    public void testThatScriptRestoreWorks() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptItemValue<Toys>(365, player.items(Toys.Collars)));
        player.item(Toys.Collar).setAvailable(true);

        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(ScriptState.UNSET, player.state.get(365));
        player.validateScripts = true;
        player.loadScript("PersistencyTest_ScriptRestoreWorks_MainScript");
        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(ScriptState.SET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

}
