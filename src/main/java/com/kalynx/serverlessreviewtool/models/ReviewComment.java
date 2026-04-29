package com.kalynx.serverlessreviewtool.models;

/**
 * ReviewComment - An inline comment on a file
 */
public class ReviewComment {
    private final String id;
    private final String filePath;
    private final int lineNumber;
    private final String author;
    private final String text;
    private final String timestamp;

    public ReviewComment(String id, String filePath, int lineNumber, String author, String text, String timestamp) {
        this.id = id;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return author + " (line " + lineNumber + "): " + text;
    }
}

