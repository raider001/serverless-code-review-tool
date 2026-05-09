package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Ada files.
 */
public class AdaSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "ada";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "abort", "abs", "abstract", "accept", "access", "aliased", "all", "and", "array", "at",
            "begin", "body", "case", "constant", "declare", "delay", "delta", "digits", "do", "else",
            "elsif", "end", "entry", "exception", "exit", "for", "function", "generic", "goto", "if",
            "in", "interface", "is", "limited", "loop", "mod", "new", "not", "null", "of", "or", "others",
            "out", "overriding", "package", "pragma", "private", "procedure", "protected", "raise", "range",
            "record", "rem", "renames", "requeue", "return", "reverse", "select", "separate", "subtype",
            "synchronized", "tagged", "task", "terminate", "then", "type", "until", "use", "when", "while",
            "with", "xor", "true", "false"
        );
    }

    @Override
    protected boolean keywordCaseInsensitive() {
        return true;
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("--[^\\n]*");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'[^']'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b\\d+(?:_?\\d)*(?:\\.\\d+(?:_?\\d)*)?(?:[eE][+-]?\\d+)?\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("=>|:=|\\*\\*|\\.\\.|[+\\-*/&=<>:;,()]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*\\.)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    }
}

