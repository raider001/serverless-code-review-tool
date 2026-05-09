package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for TOML files.
 */
public class TomlSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "toml";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("true", "false", "inf", "nan");
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("#[^\\n]*");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("[+-]?\\b\\d+(?:_?\\d)*(?:\\.\\d+(?:_?\\d)*)?(?:[eE][+-]?\\d+)?\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\[[^\\]]+\\]");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("=");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_.-]*\\b(?=\\s*=)");
    }
}

