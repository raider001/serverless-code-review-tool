package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.regex.Pattern;

/**
 * Syntax highlighter for Markdown files.
 */
public class MarkdownSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "md";
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("`[^`]*`|\\[[^\\]]+\\]\\([^\\)]+\\)");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("(?m)^```[a-zA-Z0-9_-]*$");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("(?m)^(#{1,6}\\s)|^(\\s*[-*+]\\s)|^(\\s*\\d+\\.\\s)|^(>\\s)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\*\\*[^*]+\\*\\*|\\*[^*]+\\*|_[^_]+_");
    }
}

