package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for PHP files.
 */
public class PhpSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "php";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "abstract", "and", "array", "as", "break", "callable", "case", "catch", "class", "clone", "const",
            "continue", "declare", "default", "do", "echo", "else", "elseif", "empty", "enddeclare", "endfor",
            "endforeach", "endif", "endswitch", "endwhile", "enum", "eval", "exit", "extends", "false", "final",
            "finally", "fn", "for", "foreach", "from", "function", "global", "goto", "if", "implements", "include",
            "include_once", "instanceof", "insteadof", "interface", "isset", "list", "match", "namespace", "new", "null",
            "or", "print", "private", "protected", "public", "readonly", "require", "require_once", "return", "static",
            "switch", "throw", "trait", "true", "try", "unset", "use", "var", "while", "xor", "yield"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("//[^\\n]*|#[^\\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("#\\[[^\\]]+\\]");
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
        return Pattern.compile("->|::|=>|\\?->|[+\\-*/%&|^!=<>?:]");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\$?[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*(?:->|::|\\.))");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");
    }
}

