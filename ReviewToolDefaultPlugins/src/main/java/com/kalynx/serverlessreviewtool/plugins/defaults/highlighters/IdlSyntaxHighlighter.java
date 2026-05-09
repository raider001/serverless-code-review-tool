package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for IDL files.
 */
public class IdlSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "idl";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "module", "interface", "struct", "union", "enum", "typedef", "const", "exception",
            "attribute", "readonly", "in", "out", "inout", "raises", "oneway", "void", "short",
            "long", "unsigned", "float", "double", "char", "wchar", "boolean", "octet", "string",
            "wstring", "sequence", "fixed", "any", "Object", "ValueBase", "native", "switch", "case",
            "default", "TRUE", "FALSE"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)'");
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
        return Pattern.compile("::|<<|>>|[+\\-*/%&|^!=<>?:;{},()\\[\\]]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*::)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    }
}

