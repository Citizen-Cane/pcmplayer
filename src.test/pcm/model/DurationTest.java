package pcm.model;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import teaselib.util.DurationFormat;

public class DurationTest {
    @Test
    public void testDurationToStringPositiveValues() throws Exception {
        assertEquals("00:00\"00", DurationFormat.toString(0, TimeUnit.SECONDS));
        assertEquals("00:00\"01", DurationFormat.toString(1, TimeUnit.SECONDS));
        assertEquals("00:00\"59",
                DurationFormat.toString(59, TimeUnit.SECONDS));
        assertEquals("00:01\"30",
                DurationFormat.toString(60 + 30, TimeUnit.SECONDS));
        assertEquals("00:29\"45",
                DurationFormat.toString(29 * 60 + 45, TimeUnit.SECONDS));
        assertEquals("02:42\"05", DurationFormat
                .toString(2 * 60 * 60 + 42 * 60 + 5, TimeUnit.SECONDS));
        assertEquals("13:39\"05", DurationFormat
                .toString(13 * 60 * 60 + 39 * 60 + 5, TimeUnit.SECONDS));

        assertEquals("00:01\"00", DurationFormat.toString(1, TimeUnit.MINUTES));
        assertEquals("00:29\"00",
                DurationFormat.toString(29, TimeUnit.MINUTES));
        assertEquals("02:42\"00",
                DurationFormat.toString(2 * 60 + 42, TimeUnit.MINUTES));
        assertEquals("13:39\"00",
                DurationFormat.toString(13 * 60 + 39, TimeUnit.MINUTES));
        assertEquals("INF",
                DurationFormat.toString(Long.MAX_VALUE, TimeUnit.SECONDS));
    }

    @Test
    public void testDurationToStringNegtiveValues() throws Exception {
        assertEquals("-00:00\"01",
                DurationFormat.toString(-1, TimeUnit.SECONDS));
        assertEquals("-00:00\"59",
                DurationFormat.toString(-59, TimeUnit.SECONDS));
        assertEquals("-00:01\"30",
                DurationFormat.toString(-60 - 30, TimeUnit.SECONDS));
        assertEquals("-00:29\"45",
                DurationFormat.toString(-29 * 60 - 45, TimeUnit.SECONDS));
        assertEquals("-02:42\"05", DurationFormat
                .toString(-2 * 60 * 60 - 42 * 60 - 5, TimeUnit.SECONDS));
        assertEquals("-13:39\"05", DurationFormat
                .toString(-13 * 60 * 60 - 39 * 60 - 5, TimeUnit.SECONDS));

        assertEquals("-00:01\"00",
                DurationFormat.toString(-1, TimeUnit.MINUTES));
        assertEquals("-00:29\"00",
                DurationFormat.toString(-29, TimeUnit.MINUTES));
        assertEquals("-02:42\"00",
                DurationFormat.toString(-2 * 60 - 42, TimeUnit.MINUTES));
        assertEquals("-13:39\"00",
                DurationFormat.toString(-13 * 60 - 39, TimeUnit.MINUTES));
        assertEquals("-INF",
                DurationFormat.toString(Long.MIN_VALUE, TimeUnit.SECONDS));
    }
}
