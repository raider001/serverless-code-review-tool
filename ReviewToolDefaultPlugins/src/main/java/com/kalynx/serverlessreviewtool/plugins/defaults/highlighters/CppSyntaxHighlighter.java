package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for C++ files.
 */
public class CppSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "cpp";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "alignas", "alignof", "and", "asm", "auto", "bool", "break", "case", "catch", "char", "class",
            "const", "constexpr", "continue", "default", "delete", "do", "double", "else", "enum", "explicit",
            "export", "extern", "false", "float", "for", "friend", "goto", "if", "inline", "int", "long",
            "mutable", "namespace", "new", "noexcept", "not", "nullptr", "operator", "or", "private", "protected",
            "public", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "template",
            "this", "throw", "true", "try", "typedef", "typename", "union", "unsigned", "using", "virtual", "void",
            "volatile", "while"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("R\"[\\s\\S]*?\"|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
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
    protected Pattern annotationPattern() {
        return Pattern.compile("#\\s*(include|define|ifdef|ifndef|endif|pragma)\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("::|->|<<|>>|[+\\-*/%&|^!=<>?:;,.(){}\\[\\]]");
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

