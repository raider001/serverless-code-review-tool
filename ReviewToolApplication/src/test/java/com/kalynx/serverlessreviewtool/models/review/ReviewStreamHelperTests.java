package com.kalynx.serverlessreviewtool.models.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewStreamHelperTests {

    @TempDir
    Path tempDir;

    private Path testFile;
    private static final String TEST_EDITOR = "test-editor";

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test-stream.ndjson");
    }

    @Test
    void writeTitle_thenRead_roundTripsCorrectly() throws IOException {
        String title = "Test Review Title";

        ReviewStreamHelper.writeTitle(testFile, TEST_EDITOR, title);
        List<StreamEntry<String>> entries = ReviewStreamHelper.readTitles(testFile);

        assertEquals(1, entries.size());
        assertEquals(title, entries.getFirst().data());
        assertEquals(TEST_EDITOR, entries.getFirst().editor());
    }

    @Test
    void writeDescription_thenRead_roundTripsCorrectly() throws IOException {
        String description = "This is a test review description with details.";

        ReviewStreamHelper.writeDescription(testFile, TEST_EDITOR, description);
        List<StreamEntry<String>> entries = ReviewStreamHelper.readDescriptions(testFile);

        assertEquals(1, entries.size());
        assertEquals(description, entries.getFirst().data());
    }

    @Test
    void writeAuthor_thenRead_roundTripsCorrectly() throws IOException {
        String author = "john.doe@example.com";

        ReviewStreamHelper.writeAuthor(testFile, TEST_EDITOR, author);
        List<StreamEntry<String>> entries = ReviewStreamHelper.readAuthors(testFile);

        assertEquals(1, entries.size());
        assertEquals(author, entries.getFirst().data());
    }

    @Test
    void writeStatus_thenRead_roundTripsCorrectly() throws IOException {
        String status = "pending";

        ReviewStreamHelper.writeStatus(testFile, TEST_EDITOR, status);
        List<StreamEntry<String>> entries = ReviewStreamHelper.readStatuses(testFile);

        assertEquals(1, entries.size());
        assertEquals(status, entries.getFirst().data());
    }

    @Test
    void writeCommits_thenRead_roundTripsCorrectly() throws IOException {
        List<String> commits = Arrays.asList(
            "abc123def456",
            "789ghi012jkl",
            "345mno678pqr"
        );

        ReviewStreamHelper.writeCommits(testFile, TEST_EDITOR, commits);
        List<StreamEntry<List<String>>> entries = ReviewStreamHelper.readCommits(testFile);

        assertEquals(1, entries.size());
        List<String> readCommits = entries.getFirst().data();
        assertEquals(3, readCommits.size());
        assertEquals("abc123def456", readCommits.get(0));
        assertEquals("789ghi012jkl", readCommits.get(1));
        assertEquals("345mno678pqr", readCommits.get(2));
    }

    @Test
    void writeReviewer_thenRead_roundTripsCorrectly() throws IOException {
        ReviewerData reviewer = new ReviewerData("approved", "Looks good to me!");

        ReviewStreamHelper.writeReviewer(testFile, TEST_EDITOR, reviewer);
        List<StreamEntry<ReviewerData>> entries = ReviewStreamHelper.readReviewers(testFile);

        assertEquals(1, entries.size());
        ReviewerData read = entries.getFirst().data();
        assertEquals("approved", read.getStatus());
        assertEquals("Looks good to me!", read.getSummaryComment());
    }

    @Test
    void writeComment_thenRead_roundTripsCorrectly() throws IOException {
        CommentData.CommentContext context = new CommentData.CommentContext(
            "abc123",
            "src/Main.java",
            42,
            45,
            "code snippet here"
        );
        CommentData comment = new CommentData(
            "This needs to be refactored",
            context,
            null,
            false,
            "review"
        );

        ReviewStreamHelper.writeComment(testFile, TEST_EDITOR, comment);
        List<StreamEntry<CommentData>> entries = ReviewStreamHelper.readComments(testFile);

        assertEquals(1, entries.size());
        CommentData read = entries.getFirst().data();
        assertEquals("This needs to be refactored", read.text());
        assertEquals("abc123", read.context().commit());
        assertEquals("src/Main.java", read.context().file());
        assertEquals(42, read.context().line());
        assertFalse(read.resolved());
    }

    @Test
    void writeTitle_multiple_appendsToFile() throws IOException {
        ReviewStreamHelper.writeTitle(testFile, "editor1", "Title 1");
        ReviewStreamHelper.writeTitle(testFile, "editor2", "Title 2");
        ReviewStreamHelper.writeTitle(testFile, "editor3", "Title 3");

        List<StreamEntry<String>> entries = ReviewStreamHelper.readTitles(testFile);

        assertEquals(3, entries.size());
        assertEquals("Title 1", entries.get(0).data());
        assertEquals("Title 2", entries.get(1).data());
        assertEquals("Title 3", entries.get(2).data());
        assertEquals("editor1", entries.get(0).editor());
        assertEquals("editor2", entries.get(1).editor());
        assertEquals("editor3", entries.get(2).editor());
    }

    @Test
    void writeStatus_multipleEditors_tracksEditorHistory() throws IOException {
        ReviewStreamHelper.writeStatus(testFile, "editor1", "pending");
        ReviewStreamHelper.writeStatus(testFile, "editor2", "in_progress");
        ReviewStreamHelper.writeStatus(testFile, "editor3", "approved");

        List<StreamEntry<String>> entries = ReviewStreamHelper.readStatuses(testFile);

        assertEquals(3, entries.size());
        assertEquals("pending", entries.get(0).data());
        assertEquals("in_progress", entries.get(1).data());
        assertEquals("approved", entries.get(2).data());
    }

    @Test
    void writeReviewer_multipleReviewers_storesAll() throws IOException {
        ReviewStreamHelper.writeReviewer(testFile, "editor1",
            new ReviewerData("approved", "LGTM"));
        ReviewStreamHelper.writeReviewer(testFile, "editor2",
            new ReviewerData("changes_requested", "Please address comments"));
        ReviewStreamHelper.writeReviewer(testFile, "editor3",
            new ReviewerData("approved", "Nice work"));

        List<StreamEntry<ReviewerData>> entries = ReviewStreamHelper.readReviewers(testFile);

        assertEquals(3, entries.size());
        assertEquals("approved", entries.get(0).data().getStatus());
        assertEquals("changes_requested", entries.get(1).data().getStatus());
        assertEquals("approved", entries.get(2).data().getStatus());
    }

    @Test
    void writeComment_multipleComments_maintainsOrder() throws IOException {
        for (int i = 1; i <= 5; i++) {
            CommentData comment = new CommentData(
                "Comment " + i,
                null,
                null,
                false,
                "review"
            );
            ReviewStreamHelper.writeComment(testFile, "editor", comment);
        }

        List<StreamEntry<CommentData>> entries = ReviewStreamHelper.readComments(testFile);

        assertEquals(5, entries.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("Comment " + (i + 1), entries.get(i).data().text());
        }
    }

    @Test
    void readTitles_nonExistentFile_returnsEmptyList() throws IOException {
        Path nonExistent = tempDir.resolve("nonexistent.ndjson");

        List<StreamEntry<String>> entries = ReviewStreamHelper.readTitles(nonExistent);

        assertTrue(entries.isEmpty());
    }

    @Test
    void readReviewers_emptyFile_returnsEmptyList() throws IOException {
        Files.createFile(testFile);

        List<StreamEntry<ReviewerData>> entries = ReviewStreamHelper.readReviewers(testFile);

        assertTrue(entries.isEmpty());
    }

    @Test
    void writeCommits_emptyList_writesCorrectly() throws IOException {
        List<String> emptyCommits = List.of();

        ReviewStreamHelper.writeCommits(testFile, TEST_EDITOR, emptyCommits);
        List<StreamEntry<List<String>>> entries = ReviewStreamHelper.readCommits(testFile);

        assertEquals(1, entries.size());
        assertTrue(entries.getFirst().data().isEmpty());
    }

    @Test
    void writeComment_withReplyTo_preservesReplyChain() throws IOException {
        CommentData originalComment = new CommentData(
            "Original comment",
            null,
            null,
            false,
            "review"
        );
        ReviewStreamHelper.writeComment(testFile, "editor1", originalComment);

        List<StreamEntry<CommentData>> entries = ReviewStreamHelper.readComments(testFile);
        String originalId = entries.getFirst().id();

        CommentData reply = new CommentData(
            "Reply to original",
            null,
            originalId,
            false,
            "review"
        );
        ReviewStreamHelper.writeComment(testFile, "editor2", reply);

        entries = ReviewStreamHelper.readComments(testFile);
        assertEquals(2, entries.size());
        assertEquals("Original comment", entries.get(0).data().text());
        assertEquals("Reply to original", entries.get(1).data().text());
        assertEquals(originalId, entries.get(1).data().replyTo());
    }

    @Test
    void writeComment_resolved_preservesResolvedState() throws IOException {
        CommentData comment = new CommentData(
            "This issue is resolved",
            null,
            null,
            true,
            "review"
        );

        ReviewStreamHelper.writeComment(testFile, TEST_EDITOR, comment);
        List<StreamEntry<CommentData>> entries = ReviewStreamHelper.readComments(testFile);

        assertTrue(entries.getFirst().data().resolved());
    }

    @Test
    void mixedOperations_maintainsSeparateFiles() throws IOException {
        Path titleFile = tempDir.resolve("title.ndjson");
        Path statusFile = tempDir.resolve("status.ndjson");
        Path commentFile = tempDir.resolve("comment.ndjson");

        ReviewStreamHelper.writeTitle(titleFile, "editor1", "My Title");
        ReviewStreamHelper.writeStatus(statusFile, "editor2", "pending");
        ReviewStreamHelper.writeComment(commentFile, "editor3",
            new CommentData("My comment", null, null, false, "review"));

        List<StreamEntry<String>> titles = ReviewStreamHelper.readTitles(titleFile);
        List<StreamEntry<String>> statuses = ReviewStreamHelper.readStatuses(statusFile);
        List<StreamEntry<CommentData>> comments = ReviewStreamHelper.readComments(commentFile);

        assertEquals(1, titles.size());
        assertEquals(1, statuses.size());
        assertEquals(1, comments.size());
        assertEquals("My Title", titles.getFirst().data());
        assertEquals("pending", statuses.getFirst().data());
        assertEquals("My comment", comments.getFirst().data().text());
    }

    @Test
    void writeReviewer_nullSummaryComment_handlesGracefully() throws IOException {
        ReviewerData reviewer = new ReviewerData("approved", null);

        ReviewStreamHelper.writeReviewer(testFile, TEST_EDITOR, reviewer);
        List<StreamEntry<ReviewerData>> entries = ReviewStreamHelper.readReviewers(testFile);

        assertEquals(1, entries.size());
        assertNull(entries.getFirst().data().getSummaryComment());
    }

    @Test
    void allWrites_generateUniqueIds() throws IOException {
        ReviewStreamHelper.writeTitle(testFile, TEST_EDITOR, "Title 1");
        ReviewStreamHelper.writeTitle(testFile, TEST_EDITOR, "Title 2");
        ReviewStreamHelper.writeTitle(testFile, TEST_EDITOR, "Title 3");

        List<StreamEntry<String>> entries = ReviewStreamHelper.readTitles(testFile);

        assertEquals(3, entries.size());
        String id1 = entries.get(0).id();
        String id2 = entries.get(1).id();
        String id3 = entries.get(2).id();

        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }
}

