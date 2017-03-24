package pcm.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DurationTest {
    @Test
    public void testDurationToStringPositiveValues() throws Exception {
        assertEquals("00:00\"00", Duration.toString(0));
        assertEquals("00:00\"01", Duration.toString(1000));
        assertEquals("00:00\"59", Duration.toString(1000 * 59));
        assertEquals("00:01\"30", Duration.toString(1000 * 60 + 1000 * 30));
        assertEquals("00:29\"45",
                Duration.toString(29 * 1000 * 60 + 1000 * 45));
        assertEquals("02:42\"05", Duration
                .toString(2 * 60 * 60 * 1000 + 42 * 1000 * 60 + 1000 * 5));
        assertEquals("13:39\"05", Duration
                .toString(13 * 60 * 60 * 1000 + 39 * 1000 * 60 + 1000 * 5));

        assertEquals("00:01\"00", Duration.toString(1000 * 60));
        assertEquals("00:29\"00", Duration.toString(29 * 1000 * 60));
        assertEquals("02:42\"00",
                Duration.toString(2 * 60 * 60 * 1000 + 42 * 60 * 1000));
        assertEquals("13:39\"00",
                Duration.toString(13 * 60 * 60 * 1000 + 39 * 60 * 1000));
        assertEquals("INF", Duration.toString(Long.MAX_VALUE));
    }

    @Test
    public void testDurationToStringNegtiveValues() throws Exception {
        assertEquals("-00:00\"01", Duration.toString(-1000));
        assertEquals("-00:00\"59", Duration.toString(-1000 * 59));
        assertEquals("-00:01\"30", Duration.toString(-1000 * 60 - 1000 * 30));
        assertEquals("-00:29\"45",
                Duration.toString(-29 * 1000 * 60 - 1000 * 45));
        assertEquals("-02:42\"05", Duration
                .toString(-2 * 60 * 60 * 1000 - 42 * 1000 * 60 - 1000 * 5));
        assertEquals("-13:39\"05", Duration
                .toString(-13 * 60 * 60 * 1000 - 39 * 1000 * 60 - 1000 * 5));

        assertEquals("-00:01\"00", Duration.toString(-1000 * 60));
        assertEquals("-00:29\"00", Duration.toString(-29 * 1000 * 60));
        assertEquals("-02:42\"00",
                Duration.toString(-2 * 60 * 60 * 1000 - 42 * 60 * 1000));
        assertEquals("-13:39\"00",
                Duration.toString(-13 * 60 * 60 * 1000 - 39 * 60 * 1000));
        assertEquals("-INF", Duration.toString(Long.MIN_VALUE));
    }
}
