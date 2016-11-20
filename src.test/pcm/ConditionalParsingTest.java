/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.State;
import pcm.util.TestUtils;
import teaselib.Actor;
import teaselib.Sexuality;
import teaselib.core.TeaseLib;
import teaselib.core.texttospeech.Voice.Gender;

/**
 * @author Citizen-Cane
 *
 */
public class ConditionalParsingTest {

    public Player createMistress(Sexuality.Sex sex, Sexuality.Gender gender)
            throws Exception {
        TeaseLib teaseLib = TestUtils.teaseLib();
        teaseLib.new PersistentEnum<Sexuality.Sex>(TeaseLib.DefaultDomain,
                Sexuality.Sex.class).set(sex);
        teaseLib.new PersistentEnum<Sexuality.Gender>(TeaseLib.DefaultDomain,
                Sexuality.Gender.class).set(gender);
        // TODO persistent enum needs class & default value,
        // otherwise The code looks strange
        // TODO set
        return createPlayer(teaseLib,
                teaseLib.getDominant(Gender.Female, Locale.US));
    }

    public Player createMaster(Sexuality.Sex sex, Sexuality.Gender gender)
            throws Exception {
        TeaseLib teaseLib = TestUtils.teaseLib();
        teaseLib.new PersistentEnum<Sexuality.Sex>(TeaseLib.DefaultDomain,
                Sexuality.Sex.class).set(sex);
        teaseLib.new PersistentEnum<Sexuality.Gender>(TeaseLib.DefaultDomain,
                Sexuality.Gender.class).set(gender);
        return createPlayer(teaseLib,
                teaseLib.getDominant(Gender.Male, Locale.US));
    }

    private static Player createPlayer(TeaseLib teaseLib, Actor dominant)
            throws ScriptParsingException, ValidationIssue, IOException,
            ScriptExecutionException {
        Player player = TestUtils.createPlayer(teaseLib,
                ConditionalParsingTest.class, dominant);
        player.loadScript("ConditionalParsingTest");
        assertEquals(3, player.script.actions.size());
        return player;
    }

    @Test
    public void testDominantFemalePath_If_in_If_Else() throws Exception {
        Player player = createMistress(Sexuality.Sex.Female,
                Sexuality.Gender.Feminine);
        assertEquals(Sexuality.Sex.Female,
                player.persistentEnum(Sexuality.Sex.class).value());
        assertEquals(Sexuality.Gender.Feminine,
                player.persistentEnum(Sexuality.Gender.class).value());
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if not parsed -", State.SET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else parsed -", State.UNSET, player.state.get(3));
    }

    @Test
    public void testDominantFemalePath_Else_in_If_Else() throws Exception {
        Player player = createMistress(Sexuality.Sex.Male,
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
    public void testDominantFemalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sexuality.Sex.Female,
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
    public void testDominantFemalePath_ElseIf_in_If_ElseIf_Else()
            throws Exception {
        Player player = createMistress(Sexuality.Sex.Male,
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
    public void testDominantFemalePath_Else_in_If_ElseIf_Else()
            throws Exception {
        Player player = createMistress(Sexuality.Sex.Male,
                Sexuality.Gender.Masculine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(State.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", State.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", State.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", State.SET, player.state.get(3));
    }

    @Test
    public void testDominantMalePath_If_in_If_Else() throws Exception {
        Player player = createMaster(Sexuality.Sex.Female,
                Sexuality.Gender.Feminine);
        assertEquals(Sexuality.Sex.Female,
                player.persistentEnum(Sexuality.Sex.class).value());
        assertEquals(Sexuality.Gender.Feminine,
                player.persistentEnum(Sexuality.Gender.class).value());
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
    public void testDominantMalePath_Else_in_If_Else() throws Exception {
        Player player = createMaster(Sexuality.Sex.Male,
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
    public void testDominantMalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sexuality.Sex.Female,
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
    public void testDominantMalePath_ElseIf_in_If_ElseIf_Else()
            throws Exception {
        Player player = createMaster(Sexuality.Sex.Male,
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
    public void testDominantMalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sexuality.Sex.Male,
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
