package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Rust files.
 */
public class RustSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "rs";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "as", "async", "await", "break", "const", "continue", "crate", "dyn", "else", "enum", "extern",
            "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub",
            "ref", "return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe",
            "use", "where", "while"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("r#*\"[\\s\\S]*?\"#*|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f_]+|0o[0-7_]+|0b[01_]+|\\d[\\d_]*(\\.[\\d_]+)?)\\b");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("#\\[[^\\]]+\\]");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("::|->|=>|\\.\\.|[+\\-*/%&|^!=<>?:;,.(){}\\[\\]]");
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

