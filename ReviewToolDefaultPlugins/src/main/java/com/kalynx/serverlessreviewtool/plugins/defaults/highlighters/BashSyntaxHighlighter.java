package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Bash files.
 */
public class BashSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "bash";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of("if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac", "function", "in", "select", "coproc");
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
    protected Pattern annotationPattern() {
        return Pattern.compile("(?m)^#!.*$");
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


