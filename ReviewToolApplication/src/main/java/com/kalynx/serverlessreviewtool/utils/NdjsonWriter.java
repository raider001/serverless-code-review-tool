package com.kalynx.serverlessreviewtool.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kalynx.serverlessreviewtool.models.review.StreamEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

public class NdjsonWriter {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
        .create();

    public static <T> void append(Path filePath, StreamEntry<T> entry) throws IOException {
        String json = GSON.toJson(entry);

        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )) {
            writer.write(json);
            writer.newLine();
        }
    }

    public static <T> void appendAll(Path filePath, List<StreamEntry<T>> entries) throws IOException {
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )) {
            for (StreamEntry<T> entry : entries) {
                String json = GSON.toJson(entry);
                writer.write(json);
                writer.newLine();
            }
        }
    }

    public static <T> void write(Path filePath, List<StreamEntry<T>> entries) throws IOException {
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (StreamEntry<T> entry : entries) {
                String json = GSON.toJson(entry);
                writer.write(json);
                writer.newLine();
            }
        }
    }
}

