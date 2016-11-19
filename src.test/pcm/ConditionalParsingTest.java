/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.State;
import pcm.util.TestUtils;
import teaselib.Sexuality;
import teaselib.core.TeaseLib;

/**
 * @author someone
 *
 */
public class ConditionalParsingTest {

    /**
     * @throws java.lang.Exception
     */
    public Player createPlayer(Sexuality.Sex sex, Sexuality.Gender gender)
            throws Exception {
        TeaseLib teaseLib = TestUtils.teaseLib();
        teaseLib.new PersistentEnum<Enum<?>>(TeaseLib.DefaultDomain,
                Sexuality.Sex.Male).set(sex);
        teaseLib.new PersistentEnum<Enum<?>>(TeaseLib.DefaultDomain,
                Sexuality.Gender.Masculine).set(gender);
        // TODO persistent enum needs class & default value,
        // otherwise The code looks strange
        // TODO set
        Player player = TestUtils.createPlayer(teaseLib,
                ConditionalParsingTest.class);
        player.loadScript("ConditionalParsingTest");
        assertEquals(3, player.script.actions.size());
        return player;
    }

    @Test
    public void testPath_If_in_If_Else() throws Exception {
        Player player = createPlayer(Sexuality.Sex.Female,
                Sexuality.Gender.Feminine);
        assertEquals(Sexuality.Sex.Female,
                player.persistentEnum(Sexuality.Sex.Male).value());
        assertEquals(Sexuality.Gender.Feminine,
                player.persistentEnum(Sexuality.Gender.Masculine).value());
        // todo player.get(Sexuality.Sex) to hide creation of persistent object
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if not parsed -", State.SET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else parsed -", State.UNSET, player.state.get(3));
    }

    @Test
    public void testPath_Else_in_If_Else() throws Exception {
        Player player = createPlayer(Sexuality.Sex.Male,
                Sexuality.Gender.Masculine);
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", State.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", State.SET, player.state.get(3));
    }

    @Test
    public void testPath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createPlayer(Sexuality.Sex.Female,
                Sexuality.Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals("#if not parsed -", State.SET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else parsed -", State.UNSET, player.state.get(3));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testPath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createPlayer(Sexuality.Sex.Male,
                Sexuality.Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", State.UNSET, player.state.get(1));
        assertEquals("#elseif not parsed -", State.SET, player.state.get(2));
        assertEquals("#else parsed -", State.UNSET, player.state.get(3));
    }

    @Test
    public void testPath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createPlayer(Sexuality.Sex.Male,
                Sexuality.Gender.Masculine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", State.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", State.SET, player.state.get(3));
    }
}
