package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Swift files.
 */
public class SwiftSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "swift";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import", "init",
            "inout", "internal", "let", "open", "operator", "private", "protocol", "public", "rethrows", "static",
            "struct", "subscript", "typealias", "var", "break", "case", "continue", "default", "defer", "do", "else",
            "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "where", "while", "as", "Any",
            "catch", "false", "is", "nil", "super", "self", "Self", "throw", "throws", "true", "try"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|\"([^\"\\\\]|\\\\.)*\"");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("@[A-Za-z_][A-Za-z0-9_]*");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|0b[01]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("\\.\\.|\\?\\?|[+\\-*/%&|^!=<>?:]");
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

