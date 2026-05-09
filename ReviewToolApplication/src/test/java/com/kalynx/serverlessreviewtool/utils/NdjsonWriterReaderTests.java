package com.kalynx.serverlessreviewtool.utils;

import com.kalynx.serverlessreviewtool.models.review.StreamEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NdjsonWriterReaderTests {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test.ndjson");
    }

    @Test
    void append_singleEntry_writesOneLineToFile() throws IOException {
        StreamEntry<String> entry = new StreamEntry<>(
            "test-id-001",
            Instant.parse("2026-05-03T10:00:00Z"),
            "editor1",
            "Test data"
        );

        NdjsonWriter.append(testFile, entry);

        assertTrue(Files.exists(testFile), "File should be created");
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(1, lines.size(), "Should have one line");
        assertTrue(lines.getFirst().contains("test-id-001"));
        assertTrue(lines.getFirst().contains("Test data"));
    }

    @Test
    void append_multipleEntries_appendsToExistingFile() throws IOException {
        StreamEntry<String> entry1 = new StreamEntry<>(
            "id-001",
            Instant.parse("2026-05-03T10:00:00Z"),
            "editor1",
            "First entry"
        );
        StreamEntry<String> entry2 = new StreamEntry<>(
            "id-002",
            Instant.parse("2026-05-03T10:01:00Z"),
            "editor2",
            "Second entry"
        );

        NdjsonWriter.append(testFile, entry1);
        NdjsonWriter.append(testFile, entry2);

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(2, lines.size(), "Should have two lines");
        assertTrue(lines.get(0).contains("First entry"));
        assertTrue(lines.get(1).contains("Second entry"));
    }

    @Test
    void appendAll_multipleEntries_writesAllLines() throws IOException {
        List<StreamEntry<String>> entries = Arrays.asList(
            new StreamEntry<>("id-001", Instant.parse("2026-05-03T10:00:00Z"), "editor1", "Entry 1"),
            new StreamEntry<>("id-002", Instant.parse("2026-05-03T10:01:00Z"), "editor2", "Entry 2"),
            new StreamEntry<>("id-003", Instant.parse("2026-05-03T10:02:00Z"), "editor3", "Entry 3")
        );

        NdjsonWriter.appendAll(testFile, entries);

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(3, lines.size(), "Should have three lines");
    }

    @Test
    void write_overwritesExistingFile() throws IOException {
        Files.writeString(testFile, "existing content\n");

        List<StreamEntry<String>> entries = Arrays.asList(
            new StreamEntry<>("id-001", Instant.parse("2026-05-03T10:00:00Z"), "editor1", "New Entry 1"),
            new StreamEntry<>("id-002", Instant.parse("2026-05-03T10:01:00Z"), "editor2", "New Entry 2")
        );

        NdjsonWriter.write(testFile, entries);

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(2, lines.size(), "Should overwrite with new content");
        assertTrue(lines.get(0).contains("New Entry 1"));
        assertFalse(lines.get(0).contains("existing content"));
    }

    @Test
    void append_createsDirectories() throws IOException {
        Path nestedFile = tempDir.resolve("nested/dir/test.ndjson");
        StreamEntry<String> entry = new StreamEntry<>(
            "id-001",
            Instant.parse("2026-05-03T10:00:00Z"),
            "editor1",
            "Test"
        );

        NdjsonWriter.append(nestedFile, entry);

        assertTrue(Files.exists(nestedFile), "Nested file should be created");
        assertTrue(Files.exists(nestedFile.getParent()), "Parent directories should be created");
    }

    @Test
    void read_nonExistentFile_returnsEmptyList() throws IOException {
        Path nonExistentFile = tempDir.resolve("nonexistent.ndjson");

        List<StreamEntry<String>> entries = NdjsonReader.read(nonExistentFile, String.class);

        assertTrue(entries.isEmpty(), "Should return empty list for non-existent file");
    }

    @Test
    void read_emptyFile_returnsEmptyList() throws IOException {
        Files.createFile(testFile);

        List<StreamEntry<String>> entries = NdjsonReader.read(testFile, String.class);

        assertTrue(entries.isEmpty(), "Should return empty list for empty file");
    }

    @Test
    void read_singleEntry_deserializesCorrectly() throws IOException {
        StreamEntry<String> entry = new StreamEntry<>(
            "id-001",
            Instant.parse("2026-05-03T10:00:00.123456Z"),
            "editor1",
            "Test data"
        );
        NdjsonWriter.append(testFile, entry);

        List<StreamEntry<String>> entries = NdjsonReader.read(testFile, String.class);

        assertEquals(1, entries.size());
        StreamEntry<String> read = entries.getFirst();
        assertEquals("id-001", read.id());
        assertEquals(Instant.parse("2026-05-03T10:00:00.123456Z"), read.timestamp());
        assertEquals("editor1", read.editor());
        assertEquals("Test data", read.data());
    }

    @Test
    void read_multipleEntries_deserializesAll() throws IOException {
        List<StreamEntry<String>> original = Arrays.asList(
            new StreamEntry<>("id-001", Instant.parse("2026-05-03T10:00:00Z"), "editor1", "Entry 1"),
            new StreamEntry<>("id-002", Instant.parse("2026-05-03T10:01:00Z"), "editor2", "Entry 2"),
            new StreamEntry<>("id-003", Instant.parse("2026-05-03T10:02:00Z"), "editor3", "Entry 3")
        );
        NdjsonWriter.appendAll(testFile, original);

        List<StreamEntry<String>> read = NdjsonReader.read(testFile, String.class);

        assertEquals(3, read.size());
        assertEquals("id-001", read.get(0).id());
        assertEquals("id-002", read.get(1).id());
        assertEquals("id-003", read.get(2).id());
    }

    @Test
    void read_skipsEmptyLines() throws IOException {
        Files.writeString(testFile,
            "{\"id\":\"id-001\",\"timestamp\":\"2026-05-03T10:00:00.000000Z\",\"editor\":\"editor1\",\"data\":\"Entry 1\"}\n" +
            "\n" +
            "{\"id\":\"id-002\",\"timestamp\":\"2026-05-03T10:01:00.000000Z\",\"editor\":\"editor2\",\"data\":\"Entry 2\"}\n" +
            "\n"
        );

        List<StreamEntry<String>> entries = NdjsonReader.read(testFile, String.class);

        assertEquals(2, entries.size(), "Should skip empty lines");
    }

    @Test
    void roundTrip_preservesComplexData() throws IOException {
        StreamEntry<TestData> entry = new StreamEntry<>(
            "id-001",
            Instant.parse("2026-05-03T10:00:00.123456Z"),
            "editor1",
            new TestData("test value", 42, true)
        );
        NdjsonWriter.append(testFile, entry);

        List<StreamEntry<TestData>> entries = NdjsonReader.read(testFile, TestData.class);

        assertEquals(1, entries.size());
        StreamEntry<TestData> read = entries.getFirst();
        assertEquals("id-001", read.id());
        assertEquals("test value", read.data().name);
        assertEquals(42, read.data().count);
        assertTrue(read.data().active);
    }

    @Test
    void roundTrip_listData_preservesStructure() throws IOException {
        List<String> dataList = Arrays.asList("item1", "item2", "item3");
        StreamEntry<List<String>> entry = new StreamEntry<>(
            "id-001",
            Instant.parse("2026-05-03T10:00:00Z"),
            "editor1",
            dataList
        );
        NdjsonWriter.append(testFile, entry);

        List<StreamEntry<List<String>>> entries = NdjsonReader.read(testFile,
            com.google.gson.reflect.TypeToken.getParameterized(
                StreamEntry.class,
                com.google.gson.reflect.TypeToken.getParameterized(List.class, String.class).getType()
            ).getType()
        );

        assertEquals(1, entries.size());
        StreamEntry<List<String>> read = entries.getFirst();
        assertEquals(3, read.data().size());
        assertEquals("item1", read.data().get(0));
        assertEquals("item2", read.data().get(1));
        assertEquals("item3", read.data().get(2));
    }

    @Test
    void write_preservesInsertionOrder() throws IOException {
        List<StreamEntry<Integer>> entries = Arrays.asList(
            new StreamEntry<>("id-001", Instant.parse("2026-05-03T10:00:00Z"), "editor1", 1),
            new StreamEntry<>("id-002", Instant.parse("2026-05-03T10:01:00Z"), "editor2", 2),
            new StreamEntry<>("id-003", Instant.parse("2026-05-03T10:02:00Z"), "editor3", 3),
            new StreamEntry<>("id-004", Instant.parse("2026-05-03T10:03:00Z"), "editor4", 4),
            new StreamEntry<>("id-005", Instant.parse("2026-05-03T10:04:00Z"), "editor5", 5)
        );
        NdjsonWriter.write(testFile, entries);

        List<StreamEntry<Integer>> read = NdjsonReader.read(testFile, Integer.class);

        assertEquals(5, read.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, read.get(i).data(), "Order should be preserved");
        }
    }

    @Test
    void append_largeNumberOfEntries_handlesCorrectly() throws IOException {
        int count = 1000;

        for (int i = 0; i < count; i++) {
            StreamEntry<String> entry = new StreamEntry<>(
                "id-" + String.format("%04d", i),
                Instant.parse("2026-05-03T10:00:00Z").plusSeconds(i),
                "editor",
                "Entry " + i
            );
            NdjsonWriter.append(testFile, entry);
        }

        List<StreamEntry<String>> read = NdjsonReader.read(testFile, String.class);

        assertEquals(count, read.size());
        assertEquals("Entry 0", read.get(0).data());
        assertEquals("Entry 999", read.get(999).data());
    }

    private static class TestData {
        private final String name;
        private final int count;
        private final boolean active;

        TestData(String name, int count, boolean active) {
            this.name = name;
            this.count = count;
            this.active = active;
        }
    }
}

