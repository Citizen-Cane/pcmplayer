package pcm.state.commands;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.state.StateCommandLineParameters;
import pcm.state.conditions.StateCondition;
import pcm.util.TestUtils;
import teaselib.Body;
import teaselib.Household;
import teaselib.State;
import teaselib.Toys;

public class StateConditionTest {
    final Declarations declarations = new Declarations();

    Player player;

    @Before
    public void initPlayer() throws Exception {
        player = TestUtils.createPlayer(StateCommandTest.class, "../ItemCommandTest");
    }

    public StateConditionTest() {
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);
        declarations.add("teaselib.Household", Declarations.STRING);
        declarations.add("applied.by", Declarations.STRING);
    }

    @Test
    public void testStateApply() throws Exception {
        State chastityDevice = player.state(Toys.Chastity_Device);
        State somethingOnPenis = player.state(Body.OnPenis);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "to", "teaselib.Body.OnPenis" },
                declarations));
        foo.execute(player.state);

        assertTrue(chastityDevice.applied());
        assertTrue(somethingOnPenis.applied());
        assertTrue(player.state("teaselib.Toys.Chastity_Device").applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "applied" }, declarations)).isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));

        assertTrue(new StateCondition(
                new StateCommandLineParameters(new String[] { "teaselib.Body.OnPenis", "applied" }, declarations))
                        .isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));

        chastityDevice.remove();
        assertFalse(chastityDevice.applied());
        assertFalse(somethingOnPenis.applied());
        assertFalse(player.state("teaselib.Toys.Chastity_Device").applied());
        assertFalse(player.state("teaselib.Body.OnPenis").applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "not", "applied" }, declarations))
                        .isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "is", "not", player.namespaceApplyAttribute },
                declarations)).isTrueFor(player.state));

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "not", "applied" }, declarations)).isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", "not", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));

    }

    @Test
    public void testStateApplyTo() throws Exception {
        State clothesPegs = player.state(Household.Clothes_Pegs);
        State somethingOnPenis = player.state(Body.OnPenis);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "apply", "to", "teaselib.Body.OnPenis" },
                declarations));
        foo.execute(player.state);

        assertTrue(clothesPegs.applied());
        assertTrue(somethingOnPenis.applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "applied" }, declarations)).isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));

        assertTrue(new StateCondition(
                new StateCommandLineParameters(new String[] { "teaselib.Body.OnPenis", "applied" }, declarations))
                        .isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));

        clothesPegs.remove();

        assertFalse(player.state("teaselib.Body.OnPenis").applied());
        assertFalse(clothesPegs.applied());
        assertFalse(somethingOnPenis.applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "not", "applied" }, declarations))
                        .isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "is", "not", player.namespaceApplyAttribute },
                declarations)).isTrueFor(player.state));

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "not", "applied" }, declarations)).isTrueFor(player.state));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", "not", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(player.state));
    }
}
