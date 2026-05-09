package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.regex.Pattern;

/**
 * Syntax highlighter for CSS files.
 */
public class CssSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "css";
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b\\d+(?:\\.\\d+)?(?:px|em|rem|%|vh|vw|ms|s|deg)?\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("(?m)^\\s*[.#]?[A-Za-z_][A-Za-z0-9_-]*(?=\\s*\\{)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b(?:--[A-Za-z_][A-Za-z0-9_-]*|[A-Za-z-]+)(?=\\s*:)");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[{}:;,.>#~+\\-*()]");
    }
}

