package com.kalynx.serverlessreviewtool.models;

import java.util.Objects;

/**
 * Repository - Represents a code repository in the system
 */
public class Repository {
    private final String name;
    private final String description;
    private final String url;

    public Repository(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, url);
    }
}



