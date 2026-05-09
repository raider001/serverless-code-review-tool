package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for TSX files.
 */
public class TsxSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "tsx";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "abstract", "any", "as", "async", "await", "boolean", "break", "case", "catch", "class",
            "const", "continue", "default", "delete", "do", "else", "enum", "export", "extends", "false",
            "finally", "for", "from", "function", "if", "implements", "import", "in", "instanceof",
            "interface", "let", "namespace", "never", "new", "null", "number", "private", "protected",
            "public", "readonly", "return", "static", "string", "super", "switch", "this", "throw", "true",
            "try", "type", "typeof", "undefined", "var", "void", "while"
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
    protected Pattern typePattern() {
        return Pattern.compile("(?<=</?)[A-Z][A-Za-z0-9_]*|\\b[A-Z][A-Za-z0-9_]*(?=\\s*[<(])");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("\\b[A-Za-z_:][A-Za-z0-9_.:-]*(?=\\s*=)");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("[+\\-*/%&|^!=<>?:]|=>|===|!==|==|!=|<=|>=|&&|\\|\\||\\?\\.|\\.\\.\\.|</|/>|<|>");
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


