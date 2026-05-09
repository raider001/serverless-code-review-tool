package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for JavaScript source files.
 */
public class JavaScriptSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "js";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete",
            "do", "else", "export", "extends", "false", "finally", "for", "function", "if", "import",
            "in", "instanceof", "let", "new", "null", "return", "super", "switch", "this", "throw",
            "true", "try", "typeof", "var", "void", "while", "with", "yield", "async", "await"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("`([^`\\\\]|\\\\.)*`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|0o[0-7]+|0b[01]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[+\\-*/%&|^!=<>?:]|=>|===|!==|==|!=|<=|>=|&&|\\|\\||\\?\\.|\\.\\.\\.");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[$_a-zA-Z][$_a-zA-Z0-9]*\\b(?=\\s*\\.)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[$_a-zA-Z][$_a-zA-Z0-9]*\\b");
    }
}

