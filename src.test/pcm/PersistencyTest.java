package pcm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.MappedState;
import pcm.state.State;
import pcm.util.TestUtils;
import teaselib.Toys;

public class PersistencyTest {

    @Test
    public void testThatResetRangeUnsetsMappings() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addToyMapping(MappedState.Global, 365,
                player.items(Toys.Collars));
        player.item(Toys.Collar).setAvailable(true);

        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(State.UNSET, player.state.get(365));

        player.validateScripts = false;
        player.loadScript(
                "PersistencyTest_ResetRangeUnsetsMappings_MainScript");

        // these assertions indicate that loading the script has deleted
        // the toy that has been mapped to action 365
        // TODO should be 0 and UNSET, but I've changed initializing
        assertEquals(0, player.items(Toys.Collars).available().size());
        assertEquals(State.UNSET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

    @Test
    public void testThatScriptRestoreWorks() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addToyMapping(MappedState.Global, 365,
                player.items(Toys.Collars));
        player.item(Toys.Collar).setAvailable(true);

        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(State.UNSET, player.state.get(365));
        player.validateScripts = true;
        player.loadScript("PersistencyTest_ScriptRestoreWorks_MainScript");
        assertEquals(1, player.items(Toys.Collars).available().size());
        assertEquals(State.SET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

}
