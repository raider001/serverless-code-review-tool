package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for YAML files.
 */
public class YamlSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "yaml";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("true", "false", "null", "yes", "no", "on", "off");
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("#[^\n]*");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b-?\\d+(?:\\.\\d+)?\\b");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("(?m)^\\s*(?:-\\s*)?[A-Za-z0-9_.-]+(?=\\s*:)");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[:-]");
    }
}

