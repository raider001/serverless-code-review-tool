package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Go files.
 */
public class GoSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "go";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "break", "case", "chan", "const", "continue", "default", "defer", "else", "fallthrough", "for",
            "func", "go", "goto", "if", "import", "interface", "map", "package", "range", "return", "select",
            "struct", "switch", "type", "var", "true", "false", "nil"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("`[\\s\\S]*?`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|0o[0-7]+|0b[01]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile(":=|\\.\\.\\.|[+\\-*/%&|^!=<>?:;,.(){}\\[\\]]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b(?=\\s*\\.)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b");
    }
}

