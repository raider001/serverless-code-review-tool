package com.kalynx.serverlessreviewtool.models.review;

public record CommentData(String text, CommentContext context, String replyTo, boolean resolved, String type) {

    public record CommentContext(String commit, String file, int line, int lineEnd, String codeSnippet) {
    }
}

