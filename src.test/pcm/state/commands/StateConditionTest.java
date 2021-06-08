package pcm.state.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.state.StateCommandLineParameters;
import pcm.state.conditions.StateCondition;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;
import teaselib.Body;
import teaselib.Household;
import teaselib.State;
import teaselib.Toys;
import teaselib.core.Debugger;

public class StateConditionTest {
    final Declarations declarations = new Declarations();

    Player player;
    ScriptState scriptState;

    @Before
    public void initPlayer() throws Exception {
        player = TestPlayer.loadScript(StateCommandTest.class, "ItemCommandTest");
        scriptState = player.state;
    }

    public StateConditionTest() {
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Toys", Declarations.STATE);
        declarations.add("teaselib.Body", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STATE);
        declarations.add("teaselib.Household", Declarations.STRING);
        declarations.add("teaselib.Household", Declarations.STATE);

        declarations.add("applied.by", Declarations.STRING);
    }

    @Test
    public void testStateApply() throws Exception {
        State chastityDevice = player.state(Toys.Chastity_Device);
        State somethingOnPenis = player.state(Body.OnPenis);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "to", "teaselib.Body.OnPenis" },
                declarations));
        foo.execute(scriptState);

        assertTrue(chastityDevice.applied());
        assertTrue(somethingOnPenis.applied());
        assertTrue(player.state("teaselib.Toys.Chastity_Device").applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "applied" }, declarations)).isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));

        assertTrue(new StateCondition(
                new StateCommandLineParameters(new String[] { "teaselib.Body.OnPenis", "applied" }, declarations))
                        .isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));

        chastityDevice.remove();
        assertFalse(chastityDevice.applied());
        assertFalse(somethingOnPenis.applied());
        assertFalse(player.state("teaselib.Toys.Chastity_Device").applied());
        assertFalse(player.state("teaselib.Body.OnPenis").applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "not", "applied" }, declarations))
                        .isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "is", "not", player.namespaceApplyAttribute },
                declarations)).isTrueFor(scriptState));

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "not", "applied" }, declarations)).isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", "not", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));

    }

    @Test
    public void testStateApplyTo() throws Exception {
        State clothesPegs = player.state(Household.Clothes_Pegs);
        State somethingOnPenis = player.state(Body.OnPenis);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "apply", "to", "teaselib.Body.OnPenis" },
                declarations));
        foo.execute(scriptState);

        assertTrue(clothesPegs.applied());
        assertTrue(somethingOnPenis.applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "applied" }, declarations)).isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));

        assertTrue(new StateCondition(
                new StateCommandLineParameters(new String[] { "teaselib.Body.OnPenis", "applied" }, declarations))
                        .isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));

        clothesPegs.remove();

        assertFalse(player.state("teaselib.Body.OnPenis").applied());
        assertFalse(clothesPegs.applied());
        assertFalse(somethingOnPenis.applied());

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "not", "applied" }, declarations))
                        .isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Household.Clothes_Pegs", "is", "not", player.namespaceApplyAttribute },
                declarations)).isTrueFor(scriptState));

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "not", "applied" }, declarations)).isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Body.OnPenis", "is", "not", player.namespaceApplyAttribute }, declarations))
                        .isTrueFor(scriptState));
    }

    @Test
    public void testStateRmoved() throws Exception {
        Debugger debugger = new Debugger(player);

        State analDouche = player.state(Toys.Anal_Douche);
        analDouche.apply();
        analDouche.remove();

        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Anal_Douche", "removed", "lessThan", "04:00\"00" }, declarations))
                        .isTrueFor(scriptState));

        debugger.advanceTime(4, TimeUnit.HOURS);

        assertFalse(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Anal_Douche", "removed", "lessThan", "04:00\"00" }, declarations))
                        .isTrueFor(scriptState));
        assertTrue(new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Anal_Douche", "removed", "greaterOrEqualThan", "04:00\"00" },
                declarations)).isTrueFor(scriptState));
    }

}
