package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.mockdata.GitRepositoryInitializer;
import com.kalynx.serverlessreviewtool.models.review.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GitReviewNotesManagerTests {

    @TempDir
    Path tempDir;

    private GitImpl git;
    private GitReviewNotesManager notesManager;
    private static final String REPO_NAME = "java-backend-service";
    private static final String TEST_REVIEW_ID = "01890a5d-ac96-774b-bcce-b302099a8057";
    private static final String TEST_EDITOR = "test.user@example.com";
    private Path testRepoPath;

    @BeforeAll
    static void setUpMockRepositories() {
        try {
            System.out.println("Setting up mock Git repositories for GitReviewNotesManager tests...");
            GitRepositoryInitializer.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Failed to initialize mock repositories: " + e.getMessage());
            throw new RuntimeException("Cannot run tests without mock repositories", e);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        testRepoPath = tempDir.resolve("test-repos");
        git = new GitImpl(testRepoPath);
        notesManager = new GitReviewNotesManager(git, REPO_NAME);

        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(REPO_NAME);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");
        git.cloneRepository(remoteUrl).get(30, TimeUnit.SECONDS);

        cleanupAllReviewNotes();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (testRepoPath != null && Files.exists(testRepoPath)) {
            cleanupAllReviewNotes();
            deleteDirectory(testRepoPath);
        }
    }

    private void cleanupAllReviewNotes() throws Exception {
        Path clonedRepo = testRepoPath.resolve(REPO_NAME);
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(REPO_NAME);

        if (Files.exists(clonedRepo)) {
            deleteAllNotesRefs(clonedRepo);
        }

        if (Files.exists(mockRepo)) {
            deleteAllNotesRefs(mockRepo);
        }

        Path tempReviewDir = Path.of(System.getProperty("java.io.tmpdir"))
            .resolve("git-reviews")
            .resolve(REPO_NAME);
        if (Files.exists(tempReviewDir)) {
            deleteDirectory(tempReviewDir);
        }
    }

    private void deleteAllNotesRefs(Path repoPath) throws Exception {
        Path notesDir = repoPath.resolve(".git/refs/notes/reviews");
        if (Files.exists(notesDir)) {
            try (var stream = Files.walk(notesDir)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
            }
        }
    }

    @Test
    void writeReviewTitle_newReview_writesAndPushesSuccessfully() throws Exception {
        String title = "Add user authentication feature";

        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, title)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles = notesManager.readTitles(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, titles.size());
        assertEquals(title, titles.getFirst().data());
        assertEquals(TEST_EDITOR, titles.getFirst().editor());
    }

    @Test
    void writeReviewDescription_newReview_writesCorrectly() throws Exception {
        String description = "This review adds JWT-based authentication to the user service.";

        notesManager.writeReviewDescription(TEST_REVIEW_ID, TEST_EDITOR, description)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> descriptions = notesManager.readDescriptions(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, descriptions.size());
        assertEquals(description, descriptions.getFirst().data());
    }

    @Test
    void writeReviewAuthor_newReview_writesCorrectly() throws Exception {
        String author = "john.doe@example.com";

        notesManager.writeReviewAuthor(TEST_REVIEW_ID, TEST_EDITOR, author)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> authors = notesManager.readAuthors(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, authors.size());
        assertEquals(author, authors.getFirst().data());
    }

    @Test
    void writeReviewStatus_newReview_writesCorrectly() throws Exception {
        String status = "pending";

        notesManager.writeReviewStatus(TEST_REVIEW_ID, TEST_EDITOR, status)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> statuses = notesManager.readStatuses(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, statuses.size());
        assertEquals(status, statuses.getFirst().data());
    }

    @Test
    void writeReviewCommits_newReview_writesCorrectly() throws Exception {
        List<String> commits = Arrays.asList(
            "abc123def456",
            "789ghi012jkl",
            "345mno678pqr"
        );

        notesManager.writeReviewCommits(TEST_REVIEW_ID, TEST_EDITOR, commits)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<List<String>>> commitEntries = notesManager.readCommits(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, commitEntries.size());
        List<String> readCommits = commitEntries.getFirst().data();
        assertEquals(3, readCommits.size());
        assertTrue(readCommits.contains("abc123def456"));
        assertTrue(readCommits.contains("789ghi012jkl"));
        assertTrue(readCommits.contains("345mno678pqr"));
    }

    @Test
    void writeReviewer_newReview_writesCorrectly() throws Exception {
        ReviewerData reviewer = new ReviewerData("approved", "Looks good to me!");

        notesManager.writeReviewer(TEST_REVIEW_ID, TEST_EDITOR, reviewer)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<ReviewerData>> reviewers = notesManager.readReviewers(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, reviewers.size());
        assertEquals("approved", reviewers.getFirst().data().getStatus());
        assertEquals("Looks good to me!", reviewers.getFirst().data().getSummaryComment());
    }

    @Test
    void writeComment_newReview_writesCorrectly() throws Exception {
        CommentData.CommentContext context = new CommentData.CommentContext(
            "abc123",
            "src/Main.java",
            42,
            45,
            "public static void main"
        );
        CommentData comment = new CommentData(
            "Consider using dependency injection here",
            context,
            null,
            false,
            "review"
        );

        notesManager.writeComment(TEST_REVIEW_ID, TEST_EDITOR, comment)
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<CommentData>> comments = notesManager.readComments(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, comments.size());
        assertEquals("Consider using dependency injection here", comments.getFirst().data().text());
        assertEquals("src/Main.java", comments.getFirst().data().context().file());
        assertEquals(42, comments.getFirst().data().context().line());
    }

    @Test
    void writeMultipleTitles_deduplicatesById() throws Exception {
        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "Title 1")
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "Title 2")
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "Title 3")
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles = notesManager.readTitles(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(3, titles.size(),
            "All entries with unique IDs should be preserved");
    }

    @Test
    void writeMultipleStatuses_maintainsChronologicalOrder() throws Exception {
        notesManager.writeReviewStatus(TEST_REVIEW_ID, TEST_EDITOR, "pending")
            .get(10, TimeUnit.SECONDS);
        Thread.sleep(10);
        notesManager.writeReviewStatus(TEST_REVIEW_ID, TEST_EDITOR, "in_progress")
            .get(10, TimeUnit.SECONDS);
        Thread.sleep(10);
        notesManager.writeReviewStatus(TEST_REVIEW_ID, TEST_EDITOR, "approved")
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> statuses = notesManager.readStatuses(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(3, statuses.size());
        assertTrue(statuses.get(0).timestamp().isBefore(statuses.get(1).timestamp()));
        assertTrue(statuses.get(1).timestamp().isBefore(statuses.get(2).timestamp()));
    }

    @Test
    void writeMultipleReviewers_preservesAll() throws Exception {
        notesManager.writeReviewer(TEST_REVIEW_ID, "reviewer1@example.com",
            new ReviewerData("approved", "LGTM"))
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewer(TEST_REVIEW_ID, "reviewer2@example.com",
            new ReviewerData("changes_requested", "Please address my comments"))
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewer(TEST_REVIEW_ID, "reviewer3@example.com",
            new ReviewerData("approved", "Great work!"))
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<ReviewerData>> reviewers = notesManager.readReviewers(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(3, reviewers.size());
        assertEquals("reviewer1@example.com", reviewers.get(0).editor());
        assertEquals("reviewer2@example.com", reviewers.get(1).editor());
        assertEquals("reviewer3@example.com", reviewers.get(2).editor());
    }

    @Test
    void writeMultipleComments_maintainsOrder() throws Exception {
        for (int i = 1; i <= 5; i++) {
            CommentData comment = new CommentData(
                "Comment " + i,
                null,
                null,
                false,
                "review"
            );
            notesManager.writeComment(TEST_REVIEW_ID, TEST_EDITOR, comment)
                .get(10, TimeUnit.SECONDS);
        }

        List<StreamEntry<CommentData>> comments = notesManager.readComments(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(5, comments.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("Comment " + (i + 1), comments.get(i).data().text());
        }
    }

    @Test
    void readTitles_nonExistentReview_returnsEmptyList() throws Exception {
        String nonExistentReviewId = "01890a5d-0000-0000-0000-000000000000";

        List<StreamEntry<String>> titles = notesManager.readTitles(nonExistentReviewId)
            .get(10, TimeUnit.SECONDS);

        assertTrue(titles.isEmpty());
    }

    @Test
    void writeMultipleDataTypes_maintainsSeparately() throws Exception {
        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "My Review")
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewStatus(TEST_REVIEW_ID, TEST_EDITOR, "pending")
            .get(10, TimeUnit.SECONDS);
        notesManager.writeComment(TEST_REVIEW_ID, TEST_EDITOR,
            new CommentData("A comment", null, null, false, "review"))
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles = notesManager.readTitles(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);
        List<StreamEntry<String>> statuses = notesManager.readStatuses(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);
        List<StreamEntry<CommentData>> comments = notesManager.readComments(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, titles.size());
        assertEquals(1, statuses.size());
        assertEquals(1, comments.size());
        assertEquals("My Review", titles.getFirst().data());
        assertEquals("pending", statuses.getFirst().data());
        assertEquals("A comment", comments.getFirst().data().text());
    }

    @Test
    void concurrentWrites_handleGracefully() throws Exception {
        int threadCount = 3;
        int commentsPerThread = 3;
        Thread[] threads = new Thread[threadCount];
        List<String> reviewIds = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            reviewIds.add(java.util.UUID.randomUUID().toString());
        }

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            final String reviewId = reviewIds.get(i);
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < commentsPerThread; j++) {
                        notesManager.writeComment(
                            reviewId,
                            "editor" + threadNum,
                            new CommentData("Comment from thread " + threadNum + " iteration " + j,
                                null, null, false, "review")
                        ).get(15, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    fail("Concurrent write failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join(30000);
        }

        for (int i = 0; i < threadCount; i++) {
            List<StreamEntry<CommentData>> comments = notesManager.readComments(reviewIds.get(i))
                .get(10, TimeUnit.SECONDS);
            assertEquals(commentsPerThread, comments.size(),
                "Each review should have " + commentsPerThread + " comments");
        }
    }

    @Test
    void writeAfterRead_updatesCorrectly() throws Exception {
        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "Initial Title")
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles1 = notesManager.readTitles(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);
        assertEquals(1, titles1.size());

        notesManager.writeReviewTitle(TEST_REVIEW_ID, TEST_EDITOR, "Updated Title")
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles2 = notesManager.readTitles(TEST_REVIEW_ID)
            .get(10, TimeUnit.SECONDS);
        assertEquals(2, titles2.size());
        assertEquals("Initial Title", titles2.get(0).data());
        assertEquals("Updated Title", titles2.get(1).data());
    }

    @Test
    void multipleReviewIds_storeSeparately() throws Exception {
        String reviewId1 = "01890a5d-0001-0000-0000-000000000001";
        String reviewId2 = "01890a5d-0002-0000-0000-000000000002";

        notesManager.writeReviewTitle(reviewId1, TEST_EDITOR, "Review 1 Title")
            .get(10, TimeUnit.SECONDS);
        notesManager.writeReviewTitle(reviewId2, TEST_EDITOR, "Review 2 Title")
            .get(10, TimeUnit.SECONDS);

        List<StreamEntry<String>> titles1 = notesManager.readTitles(reviewId1)
            .get(10, TimeUnit.SECONDS);
        List<StreamEntry<String>> titles2 = notesManager.readTitles(reviewId2)
            .get(10, TimeUnit.SECONDS);

        assertEquals(1, titles1.size());
        assertEquals(1, titles2.size());
        assertEquals("Review 1 Title", titles1.getFirst().data());
        assertEquals("Review 2 Title", titles2.getFirst().data());
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.exists(path.resolve(".git"))) {
            try {
                ProcessBuilder pb = new ProcessBuilder("git", "gc", "--prune=now");
                pb.directory(path.toFile());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
        }

        try (var stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                .forEach(this::deleteWithRetry);
        }
    }

    private void deleteWithRetry(Path path) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    path.toFile().setWritable(true);
                }
                Files.delete(path);
                return;
            } catch (IOException e) {
                if (i == maxRetries - 1) {
                    path.toFile().deleteOnExit();
                } else {
                    try {
                        Thread.sleep(50L * (i + 1));
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}

