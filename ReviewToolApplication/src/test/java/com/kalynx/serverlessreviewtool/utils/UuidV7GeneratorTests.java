package com.kalynx.serverlessreviewtool.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidV7GeneratorTests {

    @Test
    void generate_validFormat_returnsProperlyFormattedUuid() {
        String uuid = UuidV7Generator.generate();

        assertNotNull(uuid, "UUID should not be null");
        assertEquals(36, uuid.length(), "UUID should be 36 characters long");
        assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
            "UUID should match standard format");
    }

    @Test
    void generate_checkVersion_isVersion7() {
        String uuid = UuidV7Generator.generate();
        char versionChar = uuid.charAt(14);

        assertEquals('7', versionChar, "UUID version should be 7");
    }

    @Test
    void generate_checkVariant_isRfc4122Variant() {
        String uuid = UuidV7Generator.generate();
        char variantChar = uuid.charAt(19);

        assertTrue(variantChar == '8' || variantChar == '9' ||
                   variantChar == 'a' || variantChar == 'b',
            "UUID variant should be RFC 4122 (10xx in binary)");
    }

    @Test
    void generate_multipleGenerations_producesUniqueIds() {
        Set<String> uuids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            String uuid = UuidV7Generator.generate();
            uuids.add(uuid);
        }

        assertEquals(count, uuids.size(), "All generated UUIDs should be unique");
    }

    @Test
    void generate_sequential_maintainsTimeOrdering() throws InterruptedException {
        String uuid1 = UuidV7Generator.generate();
        Thread.sleep(2);
        String uuid2 = UuidV7Generator.generate();
        Thread.sleep(2);
        String uuid3 = UuidV7Generator.generate();

        assertTrue(uuid1.compareTo(uuid2) < 0, "UUID1 should sort before UUID2");
        assertTrue(uuid2.compareTo(uuid3) < 0, "UUID2 should sort before UUID3");
        assertTrue(uuid1.compareTo(uuid3) < 0, "UUID1 should sort before UUID3");
    }

    @Test
    void generate_isValidUuid_canBeParsedByJavaUuid() {
        String uuidString = UuidV7Generator.generate();

        assertDoesNotThrow(() -> UUID.fromString(uuidString),
            "Generated UUID should be parseable by Java's UUID class");
    }

    @Test
    void generate_timestampBitsSet_containsReasonableTimestamp() {
        long beforeMillis = System.currentTimeMillis();
        String uuidString = UuidV7Generator.generate();
        long afterMillis = System.currentTimeMillis();

        UUID uuid = UUID.fromString(uuidString);
        long timestampFromUuid = uuid.getMostSignificantBits() >>> 16;

        assertTrue(timestampFromUuid >= beforeMillis && timestampFromUuid <= afterMillis,
            "Timestamp embedded in UUID should be within reasonable range");
    }

    @Test
    void generate_rapidGeneration_handlesCollisions() {
        Set<String> uuids = new HashSet<>();
        int count = 100;

        for (int i = 0; i < count; i++) {
            uuids.add(UuidV7Generator.generate());
        }

        assertEquals(count, uuids.size(), "Rapid generation should produce unique UUIDs");
    }

    @Test
    void generate_concurrentGeneration_producesUniqueIds() throws InterruptedException {
        Set<String> uuids = new HashSet<>();
        int threadCount = 10;
        int uuidsPerThread = 100;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < uuidsPerThread; j++) {
                    synchronized (uuids) {
                        uuids.add(UuidV7Generator.generate());
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * uuidsPerThread, uuids.size(),
            "Concurrent generation should produce unique UUIDs");
    }
}

