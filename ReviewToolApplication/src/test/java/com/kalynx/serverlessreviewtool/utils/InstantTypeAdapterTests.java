package com.kalynx.serverlessreviewtool.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class InstantTypeAdapterTests {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();
    }

    @Test
    void write_validInstant_outputsUtcWithMicrosecondPrecision() {
        Instant instant = Instant.parse("2026-05-03T10:15:30.123456Z");

        String json = gson.toJson(instant);

        assertEquals("\"2026-05-03T10:15:30.123456Z\"", json);
    }

    @Test
    void write_instantWithNanoseconds_truncatesToMicroseconds() {
        Instant instant = Instant.parse("2026-05-03T10:15:30.123456789Z");

        String json = gson.toJson(instant);

        assertEquals("\"2026-05-03T10:15:30.123456Z\"", json,
            "Should truncate to 6 decimal places (microseconds)");
    }

    @Test
    void write_instantWithMilliseconds_padsToMicroseconds() {
        Instant instant = Instant.parse("2026-05-03T10:15:30.123Z");

        String json = gson.toJson(instant);

        assertEquals("\"2026-05-03T10:15:30.123000Z\"", json,
            "Should pad to 6 decimal places");
    }

    @Test
    void write_instantWithoutFractionalSeconds_padsToMicroseconds() {
        Instant instant = Instant.parse("2026-05-03T10:15:30Z");

        String json = gson.toJson(instant);

        assertEquals("\"2026-05-03T10:15:30.000000Z\"", json,
            "Should pad to 6 decimal places");
    }

    @Test
    void write_nullInstant_outputsNull() {
        Instant instant = null;

        String json = gson.toJson(instant, Instant.class);

        assertEquals("null", json);
    }

    @Test
    void write_instantInDifferentTimezone_convertsToUtc() {
        Instant instant = Instant.now();

        String json = gson.toJson(instant);

        assertTrue(json.endsWith("Z\""), "Timestamp should end with Z indicating UTC");
        assertTrue(json.contains("T"), "Timestamp should contain T separator");
    }

    @Test
    void read_validUtcTimestamp_parsesCorrectly() {
        String json = "\"2026-05-03T10:15:30.123456Z\"";

        Instant instant = gson.fromJson(json, Instant.class);

        assertEquals(Instant.parse("2026-05-03T10:15:30.123456Z"), instant);
    }

    @Test
    void read_timestampWithMilliseconds_parsesCorrectly() {
        String json = "\"2026-05-03T10:15:30.123Z\"";

        Instant instant = gson.fromJson(json, Instant.class);

        assertEquals(Instant.parse("2026-05-03T10:15:30.123Z"), instant);
    }

    @Test
    void read_timestampWithoutFractionalSeconds_parsesCorrectly() {
        String json = "\"2026-05-03T10:15:30Z\"";

        Instant instant = gson.fromJson(json, Instant.class);

        assertEquals(Instant.parse("2026-05-03T10:15:30Z"), instant);
    }

    @Test
    void roundTrip_preservesMicrosecondPrecision() {
        Instant original = Instant.parse("2026-05-03T10:15:30.123456Z");

        String json = gson.toJson(original);
        Instant deserialized = gson.fromJson(json, Instant.class);

        assertEquals(original, deserialized, "Round-trip should preserve instant value");
    }

    @Test
    void roundTrip_truncatesNanoseconds() {
        Instant original = Instant.parse("2026-05-03T10:15:30.123456789Z");
        Instant expected = Instant.parse("2026-05-03T10:15:30.123456Z");

        String json = gson.toJson(original);
        Instant deserialized = gson.fromJson(json, Instant.class);

        assertEquals(expected, deserialized,
            "Round-trip should truncate nanoseconds to microseconds");
    }

    @Test
    void write_epochInstant_formatsCorrectly() {
        Instant epoch = Instant.EPOCH;

        String json = gson.toJson(epoch);

        assertEquals("\"1970-01-01T00:00:00.000000Z\"", json);
    }

    @Test
    void write_multipleInstants_maintainsConsistentFormat() {
        Instant instant1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant instant2 = Instant.parse("2026-12-31T23:59:59.999999Z");

        String json1 = gson.toJson(instant1);
        String json2 = gson.toJson(instant2);

        assertTrue(json1.length() == json2.length(),
            "All formatted timestamps should have consistent length");
        assertTrue(json1.matches("\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z\""));
        assertTrue(json2.matches("\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}Z\""));
    }

    @Test
    void write_sortableByString_maintainsChronologicalOrder() {
        Instant earlier = Instant.parse("2026-01-01T10:00:00Z");
        Instant later = Instant.parse("2026-01-01T11:00:00Z");

        String json1 = gson.toJson(earlier);
        String json2 = gson.toJson(later);

        assertTrue(json1.compareTo(json2) < 0,
            "Earlier timestamps should sort lexicographically before later ones");
    }
}

