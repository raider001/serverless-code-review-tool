package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for PowerShell script files.
 */
public class PowerShellSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "ps1";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "if", "else", "elseif", "switch", "for", "foreach", "while", "do", "until", "break", "continue",
            "return", "function", "filter", "param", "begin", "process", "end", "class", "enum", "try", "catch",
            "finally", "throw", "trap", "in", "not", "and", "or", "xor", "true", "false", "null"
        );
    }

    @Override
    protected boolean keywordCaseInsensitive() {
        return true;
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("<#([\\s\\S]*?)#>|#[^\\n]*");
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
        return Pattern.compile("\\[[A-Za-z_][A-Za-z0-9_.]*\\]");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("-eq|-ne|-gt|-ge|-lt|-le|-like|-match|-contains|\\||=|[+\\-*/%]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\$?[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*\\.)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");
    }
}

