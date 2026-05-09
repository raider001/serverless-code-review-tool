package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Scala files.
 */
public class ScalaSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "scala";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "abstract", "case", "catch", "class", "def", "do", "else", "extends", "false", "final", "finally",
            "for", "forSome", "if", "implicit", "import", "lazy", "match", "new", "null", "object", "override",
            "package", "private", "protected", "return", "sealed", "super", "this", "throw", "trait", "try", "true",
            "type", "val", "var", "while", "with", "yield", "given", "then", "enum", "export"
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
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("=>|<-|::|\\.\\.|[+\\-*/%&|^!=<>?:]");
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

