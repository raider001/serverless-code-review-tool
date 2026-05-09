package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for SH files.
 */
public class ShSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "sh";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac", "function", "in");
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("(?m)#.*$");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b\\d+\\b");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\$\\{?[A-Za-z_][A-Za-z0-9_]*\\}?");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[|&;()<>]=?|\\$\\(|`|\\{");
    }
}


