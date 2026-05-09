package com.kalynx.serverlessreviewtool.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class InstantTypeAdapter extends TypeAdapter<Instant> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .appendInstant(6)
        .toFormatter();

    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String timestamp = value
                .atZone(ZoneOffset.UTC)
                .format(FORMATTER);
            out.value(timestamp);
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        String timestamp = in.nextString();
        return Instant.parse(timestamp);
    }
}

