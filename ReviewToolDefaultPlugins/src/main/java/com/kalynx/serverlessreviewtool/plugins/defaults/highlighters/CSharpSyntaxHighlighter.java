package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for C# files.
 */
public class CSharpSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "cs";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char", "checked",
            "class", "const", "continue", "decimal", "default", "delegate", "do", "double", "else",
            "enum", "event", "explicit", "extern", "false", "finally", "fixed", "float", "for", "foreach",
            "goto", "if", "implicit", "in", "int", "interface", "internal", "is", "lock", "long", "namespace",
            "new", "null", "object", "operator", "out", "override", "params", "private", "protected", "public",
            "readonly", "ref", "return", "sbyte", "sealed", "short", "sizeof", "stackalloc", "static", "string",
            "struct", "switch", "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked", "unsafe",
            "ushort", "using", "virtual", "void", "volatile", "while", "var", "record"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("@\"(?:[^\"]|\"\")*\"|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)'");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("\\[[A-Za-z_][A-Za-z0-9_]*(?:\\([^\\]]*\\))?\\]");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("=>|\\?\\?|::|[+\\-*/%&|^!=<>?:]");
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

