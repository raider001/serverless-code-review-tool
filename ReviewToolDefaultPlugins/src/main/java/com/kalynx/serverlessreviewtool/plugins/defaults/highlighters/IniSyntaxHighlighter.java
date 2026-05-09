package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for INI files.
 */
public class IniSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "ini";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("true", "false", "yes", "no", "on", "off");
    }

    @Override
    protected boolean keywordCaseInsensitive() {
        return true;
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("(?m)^[;#][^\\n]*$");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("[+-]?\\b\\d+(?:\\.\\d+)?\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\[[^\\]]+\\]");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[=:]");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("(?m)^\\s*[A-Za-z_][A-Za-z0-9_.-]*\\b(?=\\s*[=:])");
    }
}

