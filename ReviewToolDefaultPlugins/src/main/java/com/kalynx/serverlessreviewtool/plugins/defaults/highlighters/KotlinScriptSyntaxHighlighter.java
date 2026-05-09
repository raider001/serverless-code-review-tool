package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Kotlin script files.
 */
public class KotlinScriptSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "kts";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
            "interface", "is", "null", "object", "package", "return", "super", "this", "throw", "true",
            "try", "typealias", "val", "var", "when", "while", "by", "catch", "constructor", "delegate",
            "dynamic", "field", "file", "finally", "get", "import", "init", "param", "property", "receiver",
            "set", "setparam", "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
            "data", "enum", "expect", "external", "final", "infix", "inline", "inner", "internal", "lateinit",
            "noinline", "open", "operator", "out", "override", "private", "protected", "public", "reified",
            "sealed", "suspend", "tailrec", "vararg"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)'");
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
        return Pattern.compile("::|\\.\\.|[+\\-*/%&|^!=<>?:]");
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

