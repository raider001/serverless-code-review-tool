package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.mockdata.GitRepositoryInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitImpl demonstrating cloneRepository and removeRepository functionality.
 * Uses GitRepositoryInitializer to create mock Git repositories for testing.
 * Test naming convention: {methodName}_{scenario}_{expectedResult}
 */
public class GitImplTests {

    private static final Logger logger = LoggerFactory.getLogger(GitImplTests.class);

    @TempDir
    Path tempDir;

    private GitImpl git;
    private Path testRepoPath;

    @BeforeAll
    static void setUpMockRepositories() {
        try {
            logger.info("Setting up mock Git repositories for tests...");
            GitRepositoryInitializer.main();
        } catch (Exception e) {
            logger.error("Failed to initialize mock repositories", e);
            throw new RuntimeException("Cannot run tests without mock repositories", e);
        }
    }

    @BeforeEach
    void setUp() {
        testRepoPath = tempDir.resolve("test-repos");
        git = new GitImpl(testRepoPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testRepoPath)) {
            deleteDirectory(testRepoPath);
        }
    }

    @Test
    void cloneRepository_validRemoteUrl_createsLocalRepository() {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        CompletableFuture<Void> result = git.cloneRepository(remoteUrl);

        assertDoesNotThrow(() -> result.get(30, java.util.concurrent.TimeUnit.SECONDS));

        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo), "Cloned repository directory should exist");
        assertTrue(Files.exists(clonedRepo.resolve(".git")), ".git directory should exist");
    }


    @Test
    void cloneRepository_invalidRemoteUrl_throwsExecutionException() {
        String invalidUrl = "invalid://nonexistent.repository";

        CompletableFuture<Void> result = git.cloneRepository(invalidUrl);

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertNotNull(exception.getCause(), "Exception should have a cause");
    }

    @Test
    void cloneRepository_alreadyClonedRepository_handlesGracefully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> secondClone = git.cloneRepository(remoteUrl);

        assertDoesNotThrow(() -> secondClone.get(10, java.util.concurrent.TimeUnit.SECONDS),
            "Should handle already-cloned repository gracefully");

        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo), "Repository should still exist");
        assertTrue(Files.isDirectory(clonedRepo.resolve(".git")), "Repository should still be a valid Git repository");
    }

    @Test
    void removeRepository_existingRepository_deletesDirectory() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo), "Repository should exist before removal");

        CompletableFuture<Void> result = git.removeRepository(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertFalse(Files.exists(clonedRepo), "Repository should be deleted");
    }

    @Test
    void removeRepository_nonExistentRepository_completesSuccessfully() {
        String nonExistentRepoName = "non-existent-repo";

        CompletableFuture<Void> result = git.removeRepository(nonExistentRepoName);

        assertDoesNotThrow(() -> result.get(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void removeRepository_repositoryWithMultipleFiles_deletesAllContents() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        Path testFile = clonedRepo.resolve("test-file.txt");
        Files.writeString(testFile, "test content");
        Path testDir = clonedRepo.resolve("test-dir");
        Files.createDirectory(testDir);
        Files.writeString(testDir.resolve("nested-file.txt"), "nested content");

        CompletableFuture<Void> result = git.removeRepository(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertFalse(Files.exists(clonedRepo), "Repository and all contents should be deleted");
        assertFalse(Files.exists(testFile), "Test file should be deleted");
        assertFalse(Files.exists(testDir), "Test directory should be deleted");
    }

    @Test
    void cloneRepository_timeoutOnLongOperation_throwsTimeoutException() {
        String remoteUrl = "https://github.com/torvalds/linux.git";

        CompletableFuture<Void> result = git.cloneRepository(remoteUrl);

        assertThrows(TimeoutException.class,
            () -> result.get(1, java.util.concurrent.TimeUnit.MILLISECONDS),
            "Should timeout on very large repository clone");
    }

    @Test
    void cloneRepository_multipleRepositories_createsMultipleDirectories() {
        String repo1 = "java-backend-service";
        String repo2 = "python-api-service";
        Path mockRepo1 = GitRepositoryInitializer.getBasePath().resolve(repo1);
        Path mockRepo2 = GitRepositoryInitializer.getBasePath().resolve(repo2);
        String url1 = "file:///" + mockRepo1.toString().replace("\\", "/");
        String url2 = "file:///" + mockRepo2.toString().replace("\\", "/");

        CompletableFuture<Void> clone1 = git.cloneRepository(url1);
        CompletableFuture<Void> clone2 = git.cloneRepository(url2);

        assertDoesNotThrow(() -> CompletableFuture.allOf(clone1, clone2).get(60, java.util.concurrent.TimeUnit.SECONDS));

        assertTrue(Files.exists(testRepoPath.resolve(repo1)), "First repository should exist");
        assertTrue(Files.exists(testRepoPath.resolve(repo2)), "Second repository should exist");
    }

    @Test
    void removeRepository_immediatelyAfterClone_deletesSuccessfully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        CompletableFuture<Void> removeResult = git.removeRepository(repoName);

        assertDoesNotThrow(() -> removeResult.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertFalse(Files.exists(clonedRepo));
    }

    @Test
    void fetch_repositoryWithNotes_fetchesSuccessfully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> result = git.fetch(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo.resolve(".git/refs/notes")),
            "Notes refs should be fetched");
    }

    @Test
    void fetch_repositoryWithoutNotes_completesSuccessfully() throws Exception {
        String repoName = "python-api-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> result = git.fetch(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void fetch_nonExistentRepository_throwsExecutionException() {
        String nonExistentRepoName = "non-existent-repo";

        CompletableFuture<Void> result = git.fetch(nonExistentRepoName);

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertNotNull(exception.getCause(), "Exception should have a cause");
    }

    @Test
    void fetch_multipleRepositories_fetchesIndependently() throws Exception {
        String repo1 = "java-backend-service";
        String repo2 = "react-frontend-app";
        Path mockRepo1 = GitRepositoryInitializer.getBasePath().resolve(repo1);
        Path mockRepo2 = GitRepositoryInitializer.getBasePath().resolve(repo2);
        String url1 = "file:///" + mockRepo1.toString().replace("\\", "/");
        String url2 = "file:///" + mockRepo2.toString().replace("\\", "/");

        git.cloneRepository(url1).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.cloneRepository(url2).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> fetch1 = git.fetch(repo1);
        CompletableFuture<Void> fetch2 = git.fetch(repo2);

        assertDoesNotThrow(() -> CompletableFuture.allOf(fetch1, fetch2).get(20, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void fetch_afterClone_updatesNotes() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> fetchResult = git.fetch(repoName);

        assertDoesNotThrow(() -> fetchResult.get(10, java.util.concurrent.TimeUnit.SECONDS));
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo.resolve(".git")), "Git directory should exist");
    }

    @Test
    void pull_withNewCommits_updatesLocalRepository() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        addTestCommitToRemote(mockRepo, "test-file.txt", "Test content for pull");

        CompletableFuture<Void> result = git.pull(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo.resolve("test-file.txt")),
            "Pulled file should exist in local repository");

        removeTestCommitFromRemote(mockRepo);
    }

    @Test
    void pull_withNewNotes_updatesLocalNotes() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        String commitHash = getRemoteCommitHash(mockRepo);
        String noteRef = "refs/notes/reviews/test-review-001/metadata/title";
        String noteContent = "{\"title\":\"Test pull review\",\"timestamp\":\"2026-05-02T10:00:00Z\"}";
        addTestNoteToRemote(mockRepo, commitHash, noteRef, noteContent);

        CompletableFuture<Void> result = git.pull(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo.resolve(".git/refs/notes/reviews")),
            "Notes should be pulled to local repository");

        removeNoteFromRemote(mockRepo, commitHash, noteRef);
    }

    @Test
    void pull_nonExistentRepository_throwsExecutionException() {
        String nonExistentRepoName = "non-existent-repo";

        CompletableFuture<Void> result = git.pull(nonExistentRepoName);

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertNotNull(exception.getCause(), "Exception should have a cause");
    }

    @Test
    void pull_afterFetch_pullsAdditionalChanges() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.fetch(repoName).get(10, java.util.concurrent.TimeUnit.SECONDS);

        addTestCommitToRemote(mockRepo, "additional-file.txt", "Additional content");

        CompletableFuture<Void> pullResult = git.pull(repoName);

        assertDoesNotThrow(() -> pullResult.get(10, java.util.concurrent.TimeUnit.SECONDS));
        Path clonedRepo = testRepoPath.resolve(repoName);
        assertTrue(Files.exists(clonedRepo.resolve("additional-file.txt")),
            "File from pull should exist");

        removeTestCommitFromRemote(mockRepo);
    }

    @Test
    void pull_multipleRepositories_pullsIndependently() throws Exception {
        String repo1 = "java-backend-service";
        String repo2 = "python-api-service";
        Path mockRepo1 = GitRepositoryInitializer.getBasePath().resolve(repo1);
        Path mockRepo2 = GitRepositoryInitializer.getBasePath().resolve(repo2);
        String url1 = "file:///" + mockRepo1.toString().replace("\\", "/");
        String url2 = "file:///" + mockRepo2.toString().replace("\\", "/");

        git.cloneRepository(url1).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.cloneRepository(url2).get(30, java.util.concurrent.TimeUnit.SECONDS);

        addTestCommitToRemote(mockRepo1, "repo1-test.txt", "Repo 1 content");
        addTestCommitToRemote(mockRepo2, "repo2-test.txt", "Repo 2 content");

        CompletableFuture<Void> pull1 = git.pull(repo1);
        CompletableFuture<Void> pull2 = git.pull(repo2);

        assertDoesNotThrow(() -> CompletableFuture.allOf(pull1, pull2).get(20, java.util.concurrent.TimeUnit.SECONDS));

        assertTrue(Files.exists(testRepoPath.resolve(repo1).resolve("repo1-test.txt")));
        assertTrue(Files.exists(testRepoPath.resolve(repo2).resolve("repo2-test.txt")));

        removeTestCommitFromRemote(mockRepo1);
        removeTestCommitFromRemote(mockRepo2);
    }

    @Test
    void pull_withConflicts_completesSuccessfully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        addTestCommitToRemote(mockRepo, "conflict-test.txt", "Remote content");

        CompletableFuture<Void> result = git.pull(repoName);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        removeTestCommitFromRemote(mockRepo);
    }

    @Test
    void pushNotes_singleNote_pushesToRemote() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String commitHash = getLocalCommitHash(clonedRepo);
        String noteRef = "refs/notes/reviews/test-push-001/metadata/title";
        String noteContent = "{\"title\":\"Test push note\",\"timestamp\":\"2026-05-02T15:00:00Z\"}";

        addNoteToLocal(clonedRepo, commitHash, noteRef, noteContent);

        List<String> notes = List.of(noteRef);
        CompletableFuture<Void> result = git.pushNotes(repoName, notes);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        assertTrue(noteExistsInRemote(mockRepo, commitHash, noteRef),
            "Note should be pushed to remote repository");

        removeNoteFromRemote(mockRepo, commitHash, noteRef);
    }

    @Test
    void pushNotes_multipleNotes_pushesAllToRemote() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String commitHash = getLocalCommitHash(clonedRepo);
        String noteRef1 = "refs/notes/reviews/test-push-002/metadata/title";
        String noteRef2 = "refs/notes/reviews/test-push-002/comments";
        String noteRef3 = "refs/notes/reviews/test-push-003/metadata/status";

        addNoteToLocal(clonedRepo, commitHash, noteRef1,
            "{\"title\":\"Multi-note test\",\"timestamp\":\"2026-05-02T15:01:00Z\"}");
        addNoteToLocal(clonedRepo, commitHash, noteRef2,
            "{\"author\":\"tester\",\"comment\":\"Test comment\"}");
        addNoteToLocal(clonedRepo, commitHash, noteRef3,
            "{\"status\":\"pending\"}");

        List<String> notes = List.of(noteRef1, noteRef2, noteRef3);
        CompletableFuture<Void> result = git.pushNotes(repoName, notes);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        assertTrue(noteExistsInRemote(mockRepo, commitHash, noteRef1), "First note should be pushed");
        assertTrue(noteExistsInRemote(mockRepo, commitHash, noteRef2), "Second note should be pushed");
        assertTrue(noteExistsInRemote(mockRepo, commitHash, noteRef3), "Third note should be pushed");

        removeNoteFromRemote(mockRepo, commitHash, noteRef1);
        removeNoteFromRemote(mockRepo, commitHash, noteRef2);
        removeNoteFromRemote(mockRepo, commitHash, noteRef3);
    }

    @Test
    void pushNotes_emptyNotesList_completesSuccessfully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        List<String> emptyNotes = List.of();
        CompletableFuture<Void> result = git.pushNotes(repoName, emptyNotes);

        assertDoesNotThrow(() -> result.get(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void pushNotes_nullNotesList_completesSuccessfully() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> result = git.pushNotes(repoName, null);

        assertDoesNotThrow(() -> result.get(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void pushNotes_nonExistentRepository_throwsExecutionException() {
        String nonExistentRepoName = "non-existent-repo";
        List<String> notes = List.of("refs/notes/reviews/test/metadata");

        CompletableFuture<Void> result = git.pushNotes(nonExistentRepoName, notes);

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertNotNull(exception.getCause(), "Exception should have a cause");
    }

    @Test
    void pushNotes_afterPull_pushesNewNotes() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.pull(repoName).get(10, java.util.concurrent.TimeUnit.SECONDS);

        Path clonedRepo = testRepoPath.resolve(repoName);
        String commitHash = getLocalCommitHash(clonedRepo);
        String noteRef = "refs/notes/reviews/test-push-004/metadata/title";

        addNoteToLocal(clonedRepo, commitHash, noteRef,
            "{\"title\":\"After pull test\"}");

        List<String> notes = List.of(noteRef);
        CompletableFuture<Void> result = git.pushNotes(repoName, notes);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(noteExistsInRemote(mockRepo, commitHash, noteRef));

        removeNoteFromRemote(mockRepo, commitHash, noteRef);
    }

    @Test
    void pushNotes_multipleRepositories_pushesIndependently() throws Exception {
        String repo1 = "java-backend-service";
        String repo2 = "python-api-service";
        Path mockRepo1 = GitRepositoryInitializer.getBasePath().resolve(repo1);
        Path mockRepo2 = GitRepositoryInitializer.getBasePath().resolve(repo2);
        String url1 = "file:///" + mockRepo1.toString().replace("\\", "/");
        String url2 = "file:///" + mockRepo2.toString().replace("\\", "/");

        git.cloneRepository(url1).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.cloneRepository(url2).get(30, java.util.concurrent.TimeUnit.SECONDS);

        Path clonedRepo1 = testRepoPath.resolve(repo1);
        Path clonedRepo2 = testRepoPath.resolve(repo2);

        String commit1 = getLocalCommitHash(clonedRepo1);
        String commit2 = getLocalCommitHash(clonedRepo2);

        String noteRef1 = "refs/notes/reviews/test-push-005/metadata/title";
        String noteRef2 = "refs/notes/reviews/test-push-006/metadata/title";

        addNoteToLocal(clonedRepo1, commit1, noteRef1, "{\"title\":\"Repo1 note\"}");
        addNoteToLocal(clonedRepo2, commit2, noteRef2, "{\"title\":\"Repo2 note\"}");

        CompletableFuture<Void> push1 = git.pushNotes(repo1, List.of(noteRef1));
        CompletableFuture<Void> push2 = git.pushNotes(repo2, List.of(noteRef2));

        assertDoesNotThrow(() -> CompletableFuture.allOf(push1, push2).get(20, java.util.concurrent.TimeUnit.SECONDS));

        assertTrue(noteExistsInRemote(mockRepo1, commit1, noteRef1));
        assertTrue(noteExistsInRemote(mockRepo2, commit2, noteRef2));

        removeNoteFromRemote(mockRepo1, commit1, noteRef1);
        removeNoteFromRemote(mockRepo2, commit2, noteRef2);
    }

    @Test
    void appendToNotes_newNote_createsNoteWithData() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-append-001/comments";
        String data = "{\"author\":\"tester1\",\"comment\":\"First comment\",\"timestamp\":\"2026-05-02T15:30:00Z\"}";

        CompletableFuture<Void> result = git.appendToNotes(repoName, noteRef, data);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content = readNoteRefContent(clonedRepo, noteRef);
        assertEquals(1, content.size(), "Note should have one line");
        assertEquals(data, content.getFirst(), "Note content should match appended data");

        removeRefFromLocal(clonedRepo, noteRef);
    }

    @Test
    void appendToNotes_existingNote_appendsToExisting() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-append-002/comments";
        String data1 = "{\"author\":\"tester1\",\"comment\":\"First comment\"}";
        String data2 = "{\"author\":\"tester2\",\"comment\":\"Second comment\"}";
        String data3 = "{\"author\":\"tester3\",\"comment\":\"Third comment\"}";

        git.appendToNotes(repoName, noteRef, data1).get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef, data2).get(10, java.util.concurrent.TimeUnit.SECONDS);
        CompletableFuture<Void> result = git.appendToNotes(repoName, noteRef, data3);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content = readNoteRefContent(clonedRepo, noteRef);
        assertEquals(3, content.size(), "Note should have three lines");
        assertEquals(data1, content.getFirst(), "First line should match");
        assertEquals(data2, content.get(1), "Second line should match");
        assertEquals(data3, content.get(2), "Third line should match");

        removeRefFromLocal(clonedRepo, noteRef);
    }

    @Test
    void appendToNotes_multipleNotesInDifferentRefs_maintainsSeparately() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef1 = "refs/notes/reviews/test-append-003/comments";
        String noteRef2 = "refs/notes/reviews/test-append-003/metadata/title";
        String data1 = "{\"comment\":\"Comment in comments ref\"}";
        String data2 = "{\"title\":\"Title in metadata ref\"}";

        git.appendToNotes(repoName, noteRef1, data1).get(10, java.util.concurrent.TimeUnit.SECONDS);
        CompletableFuture<Void> result = git.appendToNotes(repoName, noteRef2, data2);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content1 = readNoteRefContent(clonedRepo, noteRef1);
        List<String> content2 = readNoteRefContent(clonedRepo, noteRef2);

        assertEquals(1, content1.size(), "First ref should have one line");
        assertEquals(1, content2.size(), "Second ref should have one line");
        assertEquals(data1, content1.getFirst());
        assertEquals(data2, content2.getFirst());

        removeRefFromLocal(clonedRepo, noteRef1);
        removeRefFromLocal(clonedRepo, noteRef2);
    }

    @Test
    void appendToNotes_nonExistentRepository_throwsExecutionException() {
        String nonExistentRepoName = "non-existent-repo";
        String noteRef = "refs/notes/reviews/test/comments";
        String data = "{\"test\":\"data\"}";

        CompletableFuture<Void> result = git.appendToNotes(nonExistentRepoName, noteRef, data);

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertNotNull(exception.getCause(), "Exception should have a cause");
    }

    @Test
    void appendToNotes_emptyData_isFilteredOut() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-append-004/comments";
        String data1 = "{\"comment\":\"First\"}";
        String data2 = "";

        git.appendToNotes(repoName, noteRef, data1).get(10, java.util.concurrent.TimeUnit.SECONDS);
        CompletableFuture<Void> result = git.appendToNotes(repoName, noteRef, data2);

        assertDoesNotThrow(() -> result.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content = readNoteRefContent(clonedRepo, noteRef);
        assertEquals(1, content.size(), "Empty lines should be filtered out in NDJSON format");
        assertEquals(data1, content.getFirst());

        removeRefFromLocal(clonedRepo, noteRef);
    }

    @Test
    void appendToNotes_multipleRepositories_appendsIndependently() throws Exception {
        String repo1 = "java-backend-service";
        String repo2 = "python-api-service";
        Path mockRepo1 = GitRepositoryInitializer.getBasePath().resolve(repo1);
        Path mockRepo2 = GitRepositoryInitializer.getBasePath().resolve(repo2);
        String url1 = "file:///" + mockRepo1.toString().replace("\\", "/");
        String url2 = "file:///" + mockRepo2.toString().replace("\\", "/");

        git.cloneRepository(url1).get(30, java.util.concurrent.TimeUnit.SECONDS);
        git.cloneRepository(url2).get(30, java.util.concurrent.TimeUnit.SECONDS);

        Path clonedRepo1 = testRepoPath.resolve(repo1);
        Path clonedRepo2 = testRepoPath.resolve(repo2);

        String noteRef1 = "refs/notes/reviews/test-append-005/comments";
        String noteRef2 = "refs/notes/reviews/test-append-006/comments";
        String data1 = "{\"repo\":\"repo1\"}";
        String data2 = "{\"repo\":\"repo2\"}";

        CompletableFuture<Void> append1 = git.appendToNotes(repo1, noteRef1, data1);
        CompletableFuture<Void> append2 = git.appendToNotes(repo2, noteRef2, data2);

        assertDoesNotThrow(() -> CompletableFuture.allOf(append1, append2).get(20, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content1 = readNoteRefContent(clonedRepo1, noteRef1);
        List<String> content2 = readNoteRefContent(clonedRepo2, noteRef2);

        assertEquals(1, content1.size());
        assertEquals(1, content2.size());
        assertEquals(data1, content1.getFirst());
        assertEquals(data2, content2.getFirst());

        removeRefFromLocal(clonedRepo1, noteRef1);
        removeRefFromLocal(clonedRepo2, noteRef2);
    }

    @Test
    void appendToNotes_thenPush_notesAppearInRemote() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        String noteRef = "refs/notes/reviews/test-append-007/comments";
        String data1 = "{\"comment\":\"First comment\"}";
        String data2 = "{\"comment\":\"Second comment\"}";

        git.appendToNotes(repoName, noteRef, data1).get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef, data2).get(10, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<Void> pushResult = git.pushNotes(repoName, List.of(noteRef));

        assertDoesNotThrow(() -> pushResult.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> remoteContent = readNoteRefContentFromRemote(mockRepo, noteRef);
        assertEquals(2, remoteContent.size(), "Remote should have two lines");
        assertEquals(data1, remoteContent.getFirst());
        assertEquals(data2, remoteContent.get(1));

        removeRefFromRemote(mockRepo, noteRef);
    }

    @Test
    void cloneRepository_setsUnionMergeStrategy() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);

        Path clonedRepo = testRepoPath.resolve(repoName);
        String mergeStrategy = getGitConfig(clonedRepo);

        assertEquals("union", mergeStrategy.trim(), "Notes merge strategy should be set to union");
    }

    @Test
    void fetch_withConflictingNotes_mergesWithUnionStrategy() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-merge-001/comments";
        String localData = "{\"comment\":\"Local comment\",\"author\":\"local-user\"}";
        String remoteData = "{\"comment\":\"Remote comment\",\"author\":\"remote-user\"}";

        git.appendToNotes(repoName, noteRef, localData).get(10, java.util.concurrent.TimeUnit.SECONDS);

        addRefContentToRemote(mockRepo, noteRef, remoteData);

        CompletableFuture<Void> fetchResult = git.fetch(repoName);

        assertDoesNotThrow(() -> fetchResult.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> mergedContent = readNoteRefContent(clonedRepo, noteRef);
        assertFalse(mergedContent.isEmpty(), "Merged content should have at least one line");
        assertTrue(mergedContent.contains(localData) || mergedContent.contains(remoteData),
            "Merged content should contain data from at least one source");

        removeRefFromLocal(clonedRepo, noteRef);
        removeRefFromRemote(mockRepo, noteRef);
    }

    @Test
    void pull_withConflictingNotes_mergesWithUnionStrategy() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-merge-002/comments";
        String localData1 = "{\"comment\":\"First local\"}";
        String localData2 = "{\"comment\":\"Second local\"}";
        String remoteData = "{\"comment\":\"Remote change\"}";

        git.appendToNotes(repoName, noteRef, localData1).get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef, localData2).get(10, java.util.concurrent.TimeUnit.SECONDS);

        addRefContentToRemote(mockRepo, noteRef, remoteData);

        CompletableFuture<Void> pullResult = git.pull(repoName);

        assertDoesNotThrow(() -> pullResult.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> mergedContent = readNoteRefContent(clonedRepo, noteRef);
        assertTrue(mergedContent.size() >= 2, "Merged content should preserve local data");

        removeRefFromLocal(clonedRepo, noteRef);
        removeRefFromRemote(mockRepo, noteRef);
    }

    @Test
    void fetch_multipleConflictingNoteRefs_mergesAllWithUnionStrategy() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef1 = "refs/notes/reviews/test-merge-003/comments";
        String noteRef2 = "refs/notes/reviews/test-merge-003/metadata/title";

        git.appendToNotes(repoName, noteRef1, "{\"local\":\"comment1\"}").get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef2, "{\"local\":\"title1\"}").get(10, java.util.concurrent.TimeUnit.SECONDS);

        addRefContentToRemote(mockRepo, noteRef1, "{\"remote\":\"comment2\"}");
        addRefContentToRemote(mockRepo, noteRef2, "{\"remote\":\"title2\"}");

        CompletableFuture<Void> fetchResult = git.fetch(repoName);

        assertDoesNotThrow(() -> fetchResult.get(10, java.util.concurrent.TimeUnit.SECONDS));

        List<String> content1 = readNoteRefContent(clonedRepo, noteRef1);
        List<String> content2 = readNoteRefContent(clonedRepo, noteRef2);

        assertFalse(content1.isEmpty(), "First ref should have merged content");
        assertFalse(content2.isEmpty(), "Second ref should have merged content");

        removeRefFromLocal(clonedRepo, noteRef1);
        removeRefFromLocal(clonedRepo, noteRef2);
        removeRefFromRemote(mockRepo, noteRef1);
        removeRefFromRemote(mockRepo, noteRef2);
    }

    @Test
    void unionMergeStrategy_preservesAllLines() throws Exception {
        String repoName = "java-backend-service";
        Path mockRepo = GitRepositoryInitializer.getBasePath().resolve(repoName);
        String remoteUrl = "file:///" + mockRepo.toString().replace("\\", "/");

        git.cloneRepository(remoteUrl).get(30, java.util.concurrent.TimeUnit.SECONDS);
        Path clonedRepo = testRepoPath.resolve(repoName);

        String noteRef = "refs/notes/reviews/test-merge-004/comments";

        git.appendToNotes(repoName, noteRef, "{\"line\":\"1-local\"}").get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef, "{\"line\":\"2-local\"}").get(10, java.util.concurrent.TimeUnit.SECONDS);
        git.appendToNotes(repoName, noteRef, "{\"line\":\"3-local\"}").get(10, java.util.concurrent.TimeUnit.SECONDS);

        addRefContentToRemote(mockRepo, noteRef, "{\"line\":\"1-remote\"}");
        addRefContentToRemote(mockRepo, noteRef, "{\"line\":\"2-remote\"}");

        git.fetch(repoName).get(10, java.util.concurrent.TimeUnit.SECONDS);

        List<String> mergedContent = readNoteRefContent(clonedRepo, noteRef);

        assertTrue(mergedContent.size() >= 3, "Union merge should preserve all local lines");

        removeRefFromLocal(clonedRepo, noteRef);
        removeRefFromRemote(mockRepo, noteRef);
    }

    private List<String> readNoteRefContent(Path repoPath, String noteRef) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "cat-file", "-p", noteRef);
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        List<String> lines = new ArrayList<>();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        process.waitFor();

        return lines;
    }

    private List<String> readNoteRefContentFromRemote(Path remotePath, String noteRef) throws Exception {
        return readNoteRefContent(remotePath, noteRef);
    }

    private void removeRefFromLocal(Path repoPath, String ref) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "update-ref", "-d", ref);
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
    }

    private void removeRefFromRemote(Path remotePath, String ref) throws Exception {
        removeRefFromLocal(remotePath, ref);
    }

    private String getGitConfig(Path repoPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "config", "--get", "notes.mergeStrategy");
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        process.waitFor();

        return output.toString();
    }

    private void addRefContentToRemote(Path remotePath, String ref, String content) throws Exception {
        Path tempFile = Files.createTempFile("ref-content", ".txt");
        try {
            List<String> existingLines = new ArrayList<>();
            try {
                existingLines = readNoteRefContent(remotePath, ref);
            } catch (Exception ignored) {
            }

            existingLines.add(content);
            Files.writeString(tempFile, String.join("\n", existingLines));

            ProcessBuilder pb = new ProcessBuilder("git", "hash-object", "-w", tempFile.toString());
            pb.directory(remotePath.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder blobHash = new StringBuilder();
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    blobHash.append(line);
                }
            }
            process.waitFor();

            pb = new ProcessBuilder("git", "update-ref", ref, blobHash.toString().trim());
            pb.directory(remotePath.toFile());
            pb.redirectErrorStream(true);
            process = pb.start();
            process.waitFor();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void addNoteToLocal(Path repoPath, String commitHash, String noteRef, String noteContent) throws Exception {
        Path tempFile = Files.createTempFile("local-note", ".txt");
        try {
            Files.writeString(tempFile, noteContent);

            ProcessBuilder pb = new ProcessBuilder("git", "notes", "--ref=" + noteRef, "add", "-F",
                tempFile.toString(), commitHash);
            pb.directory(repoPath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String getLocalCommitHash(Path repoPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
        pb.directory(repoPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        process.waitFor();

        return output.toString().trim();
    }

    private boolean noteExistsInRemote(Path remotePath, String commitHash, String noteRef) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "notes", "--ref=" + noteRef, "show", commitHash);
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        return exitCode == 0;
    }

    private void removeNoteFromRemote(Path remotePath, String commitHash, String noteRef) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "notes", "--ref=" + noteRef, "remove", commitHash);
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
    }

    private void addTestCommitToRemote(Path remotePath, String fileName, String content) throws Exception {
        Path testFile = remotePath.resolve(fileName);
        Files.writeString(testFile, content);

        ProcessBuilder pb = new ProcessBuilder("git", "add", fileName);
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();

        pb = new ProcessBuilder("git", "commit", "-m", "Test commit: " + fileName);
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);
        process = pb.start();
        process.waitFor();

        getRemoteCommitHash(remotePath);
    }

    private void addTestNoteToRemote(Path remotePath, String commitHash, String noteRef, String noteContent) throws Exception {
        Path tempFile = Files.createTempFile("test-note", ".txt");
        try {
            Files.writeString(tempFile, noteContent);

            ProcessBuilder pb = new ProcessBuilder("git", "notes", "--ref=" + noteRef, "add", "-F",
                tempFile.toString(), commitHash);
            pb.directory(remotePath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String getRemoteCommitHash(Path remotePath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        process.waitFor();

        return output.toString().trim();
    }

    private void removeTestCommitFromRemote(Path remotePath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "reset", "--hard", "HEAD~1");
        pb.directory(remotePath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
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
                    if (!path.toFile().setWritable(true)) {
                        logger.warn("Could not set file writable: {}", path);
                    }
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
