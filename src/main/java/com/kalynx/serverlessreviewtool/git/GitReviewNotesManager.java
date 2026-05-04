package com.kalynx.serverlessreviewtool.git;

import com.kalynx.serverlessreviewtool.models.review.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class GitReviewNotesManager {

    private final Git git;
    private final String repositoryName;
    private static final String NOTES_REF_PREFIX = "refs/notes/reviews/";
    private static final String REMOTE = "origin";

    public GitReviewNotesManager(Git git, String repositoryName) {
        this.git = git;
        this.repositoryName = repositoryName;
    }

    public CompletableFuture<Void> writeReviewTitle(String reviewId, String editor, String title) {
        return writeToStream(reviewId, "metadata/title",
            () -> ReviewStreamHelper.writeTitle(getStreamPath(reviewId, "metadata/title"), editor, title));
    }

    public CompletableFuture<Void> writeReviewDescription(String reviewId, String editor, String description) {
        return writeToStream(reviewId, "metadata/description",
            () -> ReviewStreamHelper.writeDescription(getStreamPath(reviewId, "metadata/description"), editor, description));
    }

    public CompletableFuture<Void> writeReviewAuthor(String reviewId, String editor, String author) {
        return writeToStream(reviewId, "metadata/author",
            () -> ReviewStreamHelper.writeAuthor(getStreamPath(reviewId, "metadata/author"), editor, author));
    }

    public CompletableFuture<Void> writeReviewStatus(String reviewId, String editor, String status) {
        return writeToStream(reviewId, "metadata/status",
            () -> ReviewStreamHelper.writeStatus(getStreamPath(reviewId, "metadata/status"), editor, status));
    }

    public CompletableFuture<Void> writeReviewCommits(String reviewId, String editor, List<String> commits) {
        return writeToStream(reviewId, "metadata/commits",
            () -> ReviewStreamHelper.writeCommits(getStreamPath(reviewId, "metadata/commits"), editor, commits));
    }

    public CompletableFuture<Void> writeReviewer(String reviewId, String editor, ReviewerData reviewerData) {
        return writeToStream(reviewId, "reviewers",
            () -> ReviewStreamHelper.writeReviewer(getStreamPath(reviewId, "reviewers"), editor, reviewerData));
    }

    public CompletableFuture<Void> writeComment(String reviewId, String editor, CommentData commentData) {
        return writeToStream(reviewId, "comments",
            () -> ReviewStreamHelper.writeComment(getStreamPath(reviewId, "comments"), editor, commentData));
    }

    public CompletableFuture<Void> createReview(String reviewId,
                                                 String editor,
                                                 String title,
                                                 String author,
                                                 String description,
                                                 String status,
                                                 List<String> commits,
                                                 List<String> reviewers,
                                                 String branch,
                                                 String baseBranch) {
        if (commits == null || commits.isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Cannot create review without commits")
            );
        }

        List<String> streamPaths = List.of(
            "metadata/title",
            "metadata/author",
            "metadata/description",
            "metadata/status",
            "metadata/commits",
            "metadata/primaryRepository",
            "metadata/branch",
            "metadata/baseBranch",
            "reviewers"
        );

        return fetchAllNotes(reviewId, streamPaths)
            .thenCompose(ignored -> getRepositoryRootCommit())
            .thenCompose(anchorCommit -> {
                try {
                    ReviewStreamHelper.writeTitle(getStreamPath(reviewId, "metadata/title"), editor, title);
                    ReviewStreamHelper.writeAuthor(getStreamPath(reviewId, "metadata/author"), editor, author);
                    ReviewStreamHelper.writeDescription(getStreamPath(reviewId, "metadata/description"), editor, description);
                    ReviewStreamHelper.writeStatus(getStreamPath(reviewId, "metadata/status"), editor, status);
                    ReviewStreamHelper.writeCommits(getStreamPath(reviewId, "metadata/commits"), editor, commits);
                    ReviewStreamHelper.writePrimaryRepository(getStreamPath(reviewId, "metadata/primaryRepository"), editor, "true");
                    ReviewStreamHelper.writeBranch(getStreamPath(reviewId, "metadata/branch"), editor, branch);
                    ReviewStreamHelper.writeBaseBranch(getStreamPath(reviewId, "metadata/baseBranch"), editor, baseBranch);

                    for (String reviewer : reviewers) {
                        ReviewerData reviewerData = new ReviewerData(
                            ReviewerData.Status.PENDING.getValue(),
                            ""
                        );
                        ReviewStreamHelper.writeReviewer(getStreamPath(reviewId, "reviewers"), reviewer, reviewerData);
                    }

                    for (String streamPath : streamPaths) {
                        resolveAndNormalize(getStreamPath(reviewId, streamPath));
                    }

                    return addAllNotesToGit(reviewId, streamPaths, anchorCommit);
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
            })
            .thenCompose(ignored -> pushAllNotes(reviewId, streamPaths));
    }

    public CompletableFuture<Void> createReviewAcrossRepositories(String reviewId,
                                                                   String editor,
                                                                   String title,
                                                                   String author,
                                                                   String description,
                                                                   String status,
                                                                   Map<String, List<String>> commitsByRepository,
                                                                   List<String> reviewers,
                                                                   List<String> repositories,
                                                                   String branch,
                                                                   String baseBranch) {
        if (repositories == null || repositories.isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Cannot create review without repositories")
            );
        }

        String primaryRepo = repositories.getFirst();
        List<String> secondaryRepos = repositories.subList(1, repositories.size());

        List<String> primaryCommits = commitsByRepository.getOrDefault(primaryRepo, List.of());

        GitReviewNotesManager primaryManager = new GitReviewNotesManager(git, primaryRepo);
        CompletableFuture<Void> primaryFuture = primaryManager.createReview(
            reviewId, editor, title, author, description, status, primaryCommits, reviewers, branch, baseBranch
        );

        if (secondaryRepos.isEmpty()) {
            return primaryFuture;
        }

        List<CompletableFuture<Void>> secondaryFutures = secondaryRepos.stream()
            .map(repoName -> {
                List<String> repoCommits = commitsByRepository.getOrDefault(repoName, List.of());
                return createSecondaryReviewReference(repoName, reviewId, editor, repoCommits, branch, baseBranch);
            })
            .toList();

        return CompletableFuture.allOf(
            Stream.concat(
                Stream.of(primaryFuture),
                secondaryFutures.stream()
            ).toArray(CompletableFuture[]::new)
        );
    }

    private CompletableFuture<Void> createSecondaryReviewReference(String repoName, String reviewId, String editor, List<String> commits, String branch, String baseBranch) {
        GitReviewNotesManager secondaryManager = new GitReviewNotesManager(git, repoName);

        List<String> streamPaths = new ArrayList<>();
        streamPaths.add("metadata/primaryRepository");
        streamPaths.add("metadata/branch");
        streamPaths.add("metadata/baseBranch");
        if (commits != null && !commits.isEmpty()) {
            streamPaths.add("metadata/commits");
        }

        return secondaryManager.fetchAllNotes(reviewId, streamPaths)
            .thenCompose(ignored -> secondaryManager.getRepositoryRootCommit())
            .thenCompose(anchorCommit -> {
                try {
                    Path primaryRepoPath = secondaryManager.getStreamPath(reviewId, "metadata/primaryRepository");
                    ReviewStreamHelper.writePrimaryRepository(primaryRepoPath, editor, "false");
                    secondaryManager.resolveAndNormalize(primaryRepoPath);

                    Path branchPath = secondaryManager.getStreamPath(reviewId, "metadata/branch");
                    ReviewStreamHelper.writeBranch(branchPath, editor, branch);
                    secondaryManager.resolveAndNormalize(branchPath);

                    Path baseBranchPath = secondaryManager.getStreamPath(reviewId, "metadata/baseBranch");
                    ReviewStreamHelper.writeBaseBranch(baseBranchPath, editor, baseBranch);
                    secondaryManager.resolveAndNormalize(baseBranchPath);

                    if (commits != null && !commits.isEmpty()) {
                        Path commitsPath = secondaryManager.getStreamPath(reviewId, "metadata/commits");
                        ReviewStreamHelper.writeCommits(commitsPath, editor, commits);
                        secondaryManager.resolveAndNormalize(commitsPath);
                    }

                    return secondaryManager.addAllNotesToGit(reviewId, streamPaths, anchorCommit);
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
            })
            .thenCompose(ignored -> secondaryManager.pushAllNotes(reviewId, streamPaths));
    }

    private CompletableFuture<String> getRepositoryRootCommit() {
        return git.executeAsync(
            repositoryName,
            "rev-list", "--max-parents=0", "HEAD"
        ).thenApply(output -> {
            String[] commits = output.trim().split("\n");
            if (commits.length == 0 || commits[0].trim().isEmpty()) {
                throw new IllegalStateException("No root commit found in repository " + repositoryName);
            }
            return commits[0].trim();
        });
    }

    private CompletableFuture<Void> fetchAllNotes(String reviewId, List<String> streamPaths) {
        List<CompletableFuture<Void>> fetchFutures = streamPaths.stream()
            .map(streamPath -> fetchAndMergeNotes(reviewId, streamPath))
            .toList();

        return CompletableFuture.allOf(fetchFutures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> addAllNotesToGit(String reviewId, List<String> streamPaths, String anchorCommit) {
        List<CompletableFuture<Void>> addFutures = streamPaths.stream()
            .map(streamPath -> addNotesToGit(reviewId, streamPath, getStreamPath(reviewId, streamPath), anchorCommit))
            .toList();

        return CompletableFuture.allOf(addFutures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> pushAllNotes(String reviewId, List<String> streamPaths) {
        List<String> refs = streamPaths.stream()
            .map(streamPath -> NOTES_REF_PREFIX + reviewId + "/" + streamPath)
            .toList();

        String[] args = new String[refs.size() + 2];
        args[0] = "push";
        args[1] = REMOTE;
        for (int i = 0; i < refs.size(); i++) {
            args[i + 2] = refs.get(i);
        }

        return git.executeAsync(repositoryName, args)
            .thenApply(ignored -> null);
    }

    private CompletableFuture<Void> writeToStream(String reviewId, String streamPath, IOOperation writer) {
        String ref = NOTES_REF_PREFIX + reviewId + "/" + streamPath;

        return fetchAndMergeNotes(reviewId, streamPath)
            .thenCompose(ignored -> getAnchorCommit(reviewId))
            .thenCompose(anchorCommit -> {
                try {
                    Path filePath = getStreamPath(reviewId, streamPath);

                    writer.execute();

                    resolveAndNormalize(filePath);

                    return addNotesToGit(reviewId, streamPath, filePath, anchorCommit);
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
            })
            .thenCompose(ignored -> pushNotes(ref));
    }

    private CompletableFuture<String> getAnchorCommit(String reviewId) {
        return getRepositoryRootCommit();
    }

    private CompletableFuture<Void> fetchAndMergeNotes(String reviewId, String streamPath) {
        String ref = NOTES_REF_PREFIX + reviewId + "/" + streamPath;

        return git.executeAsync(
            repositoryName,
            "-c", "notes.mergeStrategy=union",
            "fetch", REMOTE,
            ref + ":" + ref
        ).exceptionally(ex -> {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("couldn't find remote ref") ||
               (msg != null && msg.toLowerCase().contains("not found"))) {
                return "";
            }
            throw new RuntimeException("Failed to fetch notes: " + msg, ex);
        }).thenApply(ignored -> null);
    }

    private void resolveAndNormalize(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }

        List<String> lines = Files.readAllLines(filePath);

        Map<String, String> entriesById = new LinkedHashMap<>();
        List<EntryWithTimestamp> sortedEntries = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                String id = extractId(line);
                String timestamp = extractTimestamp(line);

                if (!entriesById.containsKey(id)) {
                    entriesById.put(id, line);
                    sortedEntries.add(new EntryWithTimestamp(id, timestamp, line));
                }
            } catch (Exception e) {
                System.err.println("Warning: Skipping malformed entry: " + line);
            }
        }

        sortedEntries.sort(Comparator.comparing(e -> e.timestamp));

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (EntryWithTimestamp entry : sortedEntries) {
                writer.write(entry.json);
                writer.newLine();
            }
        }
    }

    private String extractId(String json) {
        int idStart = json.indexOf("\"id\":\"") + 6;
        int idEnd = json.indexOf("\"", idStart);
        return json.substring(idStart, idEnd);
    }

    private String extractTimestamp(String json) {
        int tsStart = json.indexOf("\"timestamp\":\"") + 13;
        int tsEnd = json.indexOf("\"", tsStart);
        return json.substring(tsStart, tsEnd);
    }

    private CompletableFuture<Void> addNotesToGit(String reviewId, String streamPath, Path filePath, String commitHash) {
        String ref = NOTES_REF_PREFIX + reviewId + "/" + streamPath;

        return git.executeAsync(
            repositoryName,
            "notes", "--ref=" + ref, "add", "-f", "-F",
            filePath.toAbsolutePath().toString(),
            commitHash
        ).exceptionally(ex -> {
            if (ex.getMessage() != null && ex.getMessage().contains("Cannot add notes")) {
                return "";
            }
            throw new RuntimeException("Failed to add notes: " + ex.getMessage(), ex);
        }).thenApply(ignored -> null);
    }

    private CompletableFuture<Void> pushNotes(String ref) {
        return git.executeAsync(
            repositoryName,
            "push", REMOTE, ref
        ).thenApply(ignored -> null);
    }

    private Path getStreamPath(String reviewId, String streamPath) {
        return Path.of(System.getProperty("java.io.tmpdir"))
            .resolve("git-reviews")
            .resolve(repositoryName)
            .resolve(reviewId)
            .resolve(streamPath + ".ndjson");
    }

    public CompletableFuture<List<StreamEntry<String>>> readTitles(String reviewId) {
        return readStream(reviewId, "metadata/title",
                ReviewStreamHelper::readTitles);
    }

    public CompletableFuture<List<StreamEntry<String>>> readDescriptions(String reviewId) {
        return readStream(reviewId, "metadata/description",
                ReviewStreamHelper::readDescriptions);
    }

    public CompletableFuture<List<StreamEntry<String>>> readAuthors(String reviewId) {
        return readStream(reviewId, "metadata/author",
                ReviewStreamHelper::readAuthors);
    }

    public CompletableFuture<List<StreamEntry<String>>> readPrimaryRepository(String reviewId) {
        return readStream(reviewId, "metadata/primaryRepository",
                ReviewStreamHelper::readPrimaryRepository);
    }

    public CompletableFuture<List<StreamEntry<String>>> readStatuses(String reviewId) {
        return readStream(reviewId, "metadata/status",
                ReviewStreamHelper::readStatuses);
    }

    public CompletableFuture<List<StreamEntry<List<String>>>> readCommits(String reviewId) {
        return readStream(reviewId, "metadata/commits",
                ReviewStreamHelper::readCommits);
    }

    public CompletableFuture<List<StreamEntry<ReviewerData>>> readReviewers(String reviewId) {
        return readStream(reviewId, "reviewers",
                ReviewStreamHelper::readReviewers);
    }

    public CompletableFuture<List<StreamEntry<CommentData>>> readComments(String reviewId) {
        return readStream(reviewId, "comments",
                ReviewStreamHelper::readComments);
    }

    /**
     * Read all review metadata in a single batch operation for improved performance.
     * This method fetches all required notes refs at once and reads them in parallel,
     * significantly reducing the number of Git operations compared to individual reads.
     *
     * @param reviewId the review identifier
     * @return future containing all metadata (title, description, author, status, reviewers)
     */
    public CompletableFuture<ReviewMetadata> readAllMetadata(String reviewId) {
        List<String> streamPaths = List.of(
            "metadata/title",
            "metadata/description",
            "metadata/author",
            "metadata/primaryRepository",
            "metadata/branch",
            "metadata/baseBranch",
            "metadata/status",
            "reviewers"
        );

        return fetchAllNotes(reviewId, streamPaths)
            .thenCompose(ignored -> getAnchorCommit(reviewId))
            .thenCompose(anchorCommit -> {
                List<CompletableFuture<Path>> extractFutures = streamPaths.stream()
                    .map(streamPath -> extractNoteToFile(reviewId, streamPath, anchorCommit))
                    .toList();

                return CompletableFuture.allOf(extractFutures.toArray(new CompletableFuture[0]))
                    .thenApply(ignored2 -> {
                        try {
                            Path titlePath = extractFutures.get(0).join();
                            Path descPath = extractFutures.get(1).join();
                            Path authorPath = extractFutures.get(2).join();
                            Path primaryRepoPath = extractFutures.get(3).join();
                            Path branchPath = extractFutures.get(4).join();
                            Path baseBranchPath = extractFutures.get(5).join();
                            Path statusPath = extractFutures.get(6).join();
                            Path reviewersPath = extractFutures.get(7).join();

                            resolveAndNormalize(titlePath);
                            resolveAndNormalize(descPath);
                            resolveAndNormalize(authorPath);
                            resolveAndNormalize(primaryRepoPath);
                            resolveAndNormalize(branchPath);
                            resolveAndNormalize(baseBranchPath);
                            resolveAndNormalize(statusPath);
                            resolveAndNormalize(reviewersPath);

                            return new ReviewMetadata(
                                ReviewStreamHelper.readTitles(titlePath),
                                ReviewStreamHelper.readDescriptions(descPath),
                                ReviewStreamHelper.readAuthors(authorPath),
                                ReviewStreamHelper.readPrimaryRepository(primaryRepoPath),
                                ReviewStreamHelper.readBranch(branchPath),
                                ReviewStreamHelper.readBaseBranch(baseBranchPath),
                                ReviewStreamHelper.readStatuses(statusPath),
                                ReviewStreamHelper.readReviewers(reviewersPath)
                            );
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read metadata: " + e.getMessage(), e);
                        }
                    });
            });
    }

    /**
     * Container for all review metadata retrieved in a single batch operation.
     */
    public record ReviewMetadata(
        List<StreamEntry<String>> titles,
        List<StreamEntry<String>> descriptions,
        List<StreamEntry<String>> authors,
        List<StreamEntry<String>> primaryRepository,
        List<StreamEntry<String>> branches,
        List<StreamEntry<String>> baseBranches,
        List<StreamEntry<String>> statuses,
        List<StreamEntry<ReviewerData>> reviewers
    ) {}

    private <T> CompletableFuture<List<StreamEntry<T>>> readStream(
            String reviewId,
            String streamPath,
            IOReader<T> reader) {

        return fetchAndMergeNotes(reviewId, streamPath)
            .thenCompose(ignored -> getAnchorCommit(reviewId))
            .thenCompose(anchorCommit -> extractNoteToFile(reviewId, streamPath, anchorCommit))
            .thenApply(filePath -> {
                try {
                    resolveAndNormalize(filePath);
                    return reader.read(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read stream: " + e.getMessage(), e);
                }
            });
    }

    private CompletableFuture<Path> extractNoteToFile(String reviewId, String streamPath, String commitHash) {
        String ref = NOTES_REF_PREFIX + reviewId + "/" + streamPath;
        Path filePath = getStreamPath(reviewId, streamPath);

        return git.executeAsync(
            repositoryName,
            "notes", "--ref=" + ref, "show", commitHash
        ).thenApply(content -> {
            try {
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, content);
                return filePath;
            } catch (IOException e) {
                throw new RuntimeException("Failed to write note content to file: " + e.getMessage(), e);
            }
        }).exceptionally(ex -> {
            if (ex.getMessage() != null && ex.getMessage().contains("no note found")) {
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.writeString(filePath, "");
                    return filePath;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create empty file: " + e.getMessage(), e);
                }
            }
            throw new RuntimeException("Failed to extract note: " + ex.getMessage(), ex);
        });
    }

    @FunctionalInterface
    private interface IOOperation {
        void execute() throws IOException;
    }

    @FunctionalInterface
    private interface IOReader<T> {
        List<StreamEntry<T>> read(Path path) throws IOException;
    }

    private record EntryWithTimestamp(String id, String timestamp, String json) {}
}





