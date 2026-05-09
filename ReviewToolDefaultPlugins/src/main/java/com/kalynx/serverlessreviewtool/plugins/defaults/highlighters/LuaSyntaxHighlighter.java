package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Lua files.
 */
public class LuaSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "lua";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "goto", "if", "in",
            "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("--\\[\\[[\\s\\S]*?\\]\\]|--[^\\n]*");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\\[\\[[\\s\\S]*?\\]\\]|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("\\.\\.|[+\\-*/%^#=<>~:]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*[.:])");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    }
}

