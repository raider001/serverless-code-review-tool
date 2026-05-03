package com.kalynx.serverlessreviewtool.models.review;

import com.kalynx.serverlessreviewtool.utils.UuidV7Generator;

import java.time.Instant;

public record StreamEntry<T>(String id, Instant timestamp, String editor, T data) {

    public static <T> StreamEntry<T> create(String editor, T data) {
        return new StreamEntry<>(
                UuidV7Generator.generate(),
                Instant.now(),
                editor,
                data
        );
    }
}

