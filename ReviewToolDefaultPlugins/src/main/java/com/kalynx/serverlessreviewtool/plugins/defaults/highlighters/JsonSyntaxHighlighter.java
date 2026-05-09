package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for JSON files.
 */
public class JsonSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "json";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("true", "false", "null");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"(?=\\s*:)");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[{}\\[\\]:,]");
    }
}

