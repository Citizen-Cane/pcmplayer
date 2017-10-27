package pcm.state;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class StateCommandLineParametersTest {

    @Test
    public void testParameterNormalizationForApplied() {
        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"),
                StateCommandLineParameters.normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "applied")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "applied")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "applied", "to", "foo.bar"),
                StateCommandLineParameters.normalizedArgs(
                        Arrays.asList("teaselib.Toys.Chastity_Device", "not", "applied", "to", "foo.bar")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "applied", "to", "foo.bar"),
                StateCommandLineParameters.normalizedArgs(
                        Arrays.asList("teaselib.Toys.Chastity_Device", "is", "not", "applied", "to", "foo.bar")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "applied", "to", "foo.bar"),
                StateCommandLineParameters.normalizedArgs(
                        Arrays.asList("teaselib.Toys.Chastity_Device", "isnt", "applied", "to", "foo.bar")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "applied", "to", "foo.bar"),
                StateCommandLineParameters.normalizedArgs(
                        Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "applied", "to", "foo.bar")));
    }

    @Test
    public void testParameterNormalizationForFree() {
        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "free"),
                StateCommandLineParameters.normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "free")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "free"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "free")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "free")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "not", "free")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "isnt", "free")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "applied"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "free")));
    }

    @Test
    public void testParameterNormalizationForAttributes() {
        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "applied.by.me"), StateCommandLineParameters
                .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "applied.by.me")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "applied.by.me"),
                StateCommandLineParameters
                        .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "applied.by.me")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "applied.by.me"),
                StateCommandLineParameters
                        .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "is", "not", "applied.by.me")));

        assertEquals(Arrays.asList("teaselib.Toys.Chastity_Device", "not", "is", "applied.by.me"),
                StateCommandLineParameters
                        .normalizedArgs(Arrays.asList("teaselib.Toys.Chastity_Device", "isnt", "applied.by.me")));
    }
}
