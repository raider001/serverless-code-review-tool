package com.kalynx.serverlessreviewtool.ui.review.model;

/**
 * Commit - Represents a Git commit
 */
public class Commit {
    private final String hash;
    private final String message;
    private final String author;
    private final String date;

    public Commit(String hash, String message, String author, String date) {
        this.hash = hash;
        this.message = message;
        this.author = author;
        this.date = date;
    }

    public String getHash() {
        return hash;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getShortHash() {
        return hash.length() > 7 ? hash.substring(0, 7) : hash;
    }

    @Override
    public String toString() {
        return getShortHash() + " - " + message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Commit)) return false;
        Commit other = (Commit) obj;
        return hash.equals(other.hash);
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }
}

