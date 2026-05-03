package com.kalynx.serverlessreviewtool.models.review;

import com.google.gson.reflect.TypeToken;
import com.kalynx.serverlessreviewtool.utils.NdjsonReader;
import com.kalynx.serverlessreviewtool.utils.NdjsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;

public class ReviewStreamHelper {

    private static <T> void writeEntry(Path filePath, String editor, T data) throws IOException {
        StreamEntry<T> entry = StreamEntry.create(editor, data);
        NdjsonWriter.append(filePath, entry);
    }

    private static <T> List<StreamEntry<T>> readEntries(Path filePath, Class<T> dataType) throws IOException {
        return NdjsonReader.read(filePath, dataType);
    }

    public static void writeTitle(Path filePath, String editor, String title) throws IOException {
        writeEntry(filePath, editor, title);
    }

    public static List<StreamEntry<String>> readTitles(Path filePath) throws IOException {
        return readEntries(filePath, String.class);
    }

    public static void writeDescription(Path filePath, String editor, String description) throws IOException {
        writeEntry(filePath, editor, description);
    }

    public static List<StreamEntry<String>> readDescriptions(Path filePath) throws IOException {
        return readEntries(filePath, String.class);
    }

    public static void writeAuthor(Path filePath, String editor, String author) throws IOException {
        writeEntry(filePath, editor, author);
    }

    public static List<StreamEntry<String>> readAuthors(Path filePath) throws IOException {
        return readEntries(filePath, String.class);
    }

    public static void writeStatus(Path filePath, String editor, String status) throws IOException {
        writeEntry(filePath, editor, status);
    }

    public static List<StreamEntry<String>> readStatuses(Path filePath) throws IOException {
        return readEntries(filePath, String.class);
    }

    public static void writeCommits(Path filePath, String editor, List<String> commits) throws IOException {
        writeEntry(filePath, editor, commits);
    }

    public static List<StreamEntry<List<String>>> readCommits(Path filePath) throws IOException {
        Type listStringType = new TypeToken<List<String>>() {}.getType();
        Type entryType = TypeToken.getParameterized(StreamEntry.class, listStringType).getType();
        return NdjsonReader.read(filePath, entryType);
    }

    public static void writeReviewer(Path filePath, String editor, ReviewerData reviewerData) throws IOException {
        writeEntry(filePath, editor, reviewerData);
    }

    public static List<StreamEntry<ReviewerData>> readReviewers(Path filePath) throws IOException {
        return readEntries(filePath, ReviewerData.class);
    }

    public static void writeComment(Path filePath, String editor, CommentData commentData) throws IOException {
        writeEntry(filePath, editor, commentData);
    }

    public static List<StreamEntry<CommentData>> readComments(Path filePath) throws IOException {
        return readEntries(filePath, CommentData.class);
    }
}





