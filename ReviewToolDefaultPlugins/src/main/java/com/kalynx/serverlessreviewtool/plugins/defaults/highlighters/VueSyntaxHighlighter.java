package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Vue single-file components.
 */
public class VueSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "vue";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "template", "script", "style", "setup", "export", "default", "import", "from", "const", "let",
            "if", "else", "for", "return", "true", "false", "null", "computed", "watch", "ref", "reactive"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("<!--([\\s\\S]*?)-->|//[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("`([^`\\\\]|\\\\.)*`|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b(0x[0-9A-Fa-f]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("(?<=</?)[A-Za-z][A-Za-z0-9:-]*");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("v-[a-z-]+|:[A-Za-z_][A-Za-z0-9_-]*|@[A-Za-z_][A-Za-z0-9_-]*");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("</|/>|<|>|=|=>|===|!==|==|!=|<=|>=|&&|\\|\\||\\?\\.|\\.\\.\\.");
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

