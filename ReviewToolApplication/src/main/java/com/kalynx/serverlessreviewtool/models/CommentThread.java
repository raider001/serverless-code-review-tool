package com.kalynx.serverlessreviewtool.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentThread - Organizes flat comment list into threaded structure
 * Handles conversion from flat storage to hierarchical display
 */
public class CommentThread {
    private final ReviewComment rootComment;
    private final List<CommentThread> replies;

    public CommentThread(ReviewComment rootComment) {
        this.rootComment = rootComment;
        this.replies = new ArrayList<>();
    }

    public ReviewComment getComment() {
        return rootComment;
    }

    public List<CommentThread> getReplies() {
        return replies;
    }

    public void addReply(CommentThread reply) {
        replies.add(reply);
    }

    public boolean hasReplies() {
        return !replies.isEmpty();
    }

    public int getTotalCommentCount() {
        int count = 1;
        for (CommentThread reply : replies) {
            count += reply.getTotalCommentCount();
        }
        return count;
    }

    public boolean hasUnresolvedComments() {
        if (rootComment.needsResolution() && !rootComment.isResolved()) {
            return true;
        }
        for (CommentThread reply : replies) {
            if (reply.hasUnresolvedComments()) {
                return true;
            }
        }
        return false;
    }

    public static List<CommentThread> organizeComments(List<ReviewComment> flatComments) {
        List<CommentThread> threads = new ArrayList<>();

        List<ReviewComment> rootComments = flatComments.stream()
            .filter(c -> c.getParentId() == null)
            .collect(Collectors.toList());

        for (ReviewComment root : rootComments) {
            CommentThread thread = new CommentThread(root);
            buildThread(thread, flatComments);
            threads.add(thread);
        }

        threads.sort(Comparator.comparingInt(t -> t.getComment().getLineNumber()));

        return threads;
    }

    private static void buildThread(CommentThread thread, List<ReviewComment> allComments) {
        String parentId = thread.getComment().getId();

        List<ReviewComment> replies = allComments.stream()
            .filter(c -> parentId.equals(c.getParentId()))
            .collect(Collectors.toList());

        for (ReviewComment reply : replies) {
            CommentThread replyThread = new CommentThread(reply);
            buildThread(replyThread, allComments);
            thread.addReply(replyThread);
        }
    }
}

