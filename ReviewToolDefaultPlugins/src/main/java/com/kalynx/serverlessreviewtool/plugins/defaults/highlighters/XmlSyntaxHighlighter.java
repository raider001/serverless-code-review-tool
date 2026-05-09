package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.regex.Pattern;

/**
 * Syntax highlighter for XML files.
 */
public class XmlSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "xml";
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("<!--([\\s\\S]*?)-->");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("(?<=</?)[A-Za-z_][A-Za-z0-9_.:-]*");
    }

    @Override
    protected Pattern annotationPattern() {
        return Pattern.compile("\\b[A-Za-z_:][A-Za-z0-9_.:-]*(?=\\s*=)");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("</|/>|<|>|=|\\?|:");
    }
}

