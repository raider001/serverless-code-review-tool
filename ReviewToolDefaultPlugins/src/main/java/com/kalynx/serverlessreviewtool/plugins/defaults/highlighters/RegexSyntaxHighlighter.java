package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class RegexSyntaxHighlighter extends SyntaxHighlighterPlugin {

    @Override
    public final String getFileExtension() {
        return extension();
    }

    @Override
    public final List<SyntaxToken> tokenize(String source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<SyntaxToken> tokens = new ArrayList<>();
        boolean[] covered = new boolean[source.length()];

        addTokens(tokens, covered, source, commentPattern(), TokenType.COMMENT);
        addTokens(tokens, covered, source, stringPattern(), TokenType.STRING);
        addTokens(tokens, covered, source, annotationPattern(), TokenType.ANNOTATION);
        addTokens(tokens, covered, source, numberPattern(), TokenType.NUMBER);
        addTokens(tokens, covered, source, typePattern(), TokenType.TYPE);
        addTokens(tokens, covered, source, keywordPattern(), TokenType.KEYWORD);
        addTokens(tokens, covered, source, operatorPattern(), TokenType.OPERATOR);
        addTokens(tokens, covered, source, objectPattern(), TokenType.OBJECT);
        addTokens(tokens, covered, source, variablePattern(), TokenType.VARIABLE);

        return tokens;
    }

    @Override
    public final Color getColorForTokenType(TokenType type, boolean darkTheme) {
        if (darkTheme) {
            return switch (type) {
                case KEYWORD -> new Color(86, 156, 214);
                case STRING -> new Color(206, 145, 120);
                case COMMENT -> new Color(106, 153, 85);
                case NUMBER -> new Color(181, 206, 168);
                case TYPE -> new Color(78, 201, 176);
                case ANNOTATION -> new Color(220, 220, 170);
                case OPERATOR -> new Color(212, 212, 212);
                case OBJECT -> new Color(156, 180, 254);
                case VARIABLE -> new Color(156, 220, 254);
                case DEFAULT -> new Color(220, 220, 220);
            };
        }

        return switch (type) {
            case KEYWORD -> new Color(0, 0, 170);
            case STRING -> new Color(163, 21, 21);
            case COMMENT -> new Color(0, 128, 0);
            case NUMBER -> new Color(9, 134, 88);
            case TYPE -> new Color(43, 145, 175);
            case ANNOTATION -> new Color(111, 66, 193);
            case OPERATOR -> new Color(32, 32, 32);
            case OBJECT -> new Color(0, 92, 197);
            case VARIABLE -> new Color(121, 94, 38);
            case DEFAULT -> new Color(30, 30, 30);
        };
    }

    protected abstract String extension();

    protected Pattern commentPattern() {
        return null;
    }

    protected Pattern stringPattern() {
        return null;
    }

    protected Pattern annotationPattern() {
        return null;
    }

    protected Pattern numberPattern() {
        return null;
    }

    protected Pattern typePattern() {
        return null;
    }

    protected Pattern operatorPattern() {
        return null;
    }

    protected Pattern objectPattern() {
        return null;
    }

    protected Pattern variablePattern() {
        return null;
    }

    protected Set<String> keywords() {
        return Set.of();
    }

    protected boolean keywordCaseInsensitive() {
        return false;
    }

    private Pattern keywordPattern() {
        Set<String> words = keywords();
        if (words == null || words.isEmpty()) {
            return null;
        }
        String body = String.join("|", words.stream().map(Pattern::quote).toList());
        String regex = "\\b(" + body + ")\\b";
        if (keywordCaseInsensitive()) {
            regex = "(?i)" + regex;
        }
        return Pattern.compile(regex);
    }

    private void addTokens(List<SyntaxToken> tokens, boolean[] covered,
                           String source, Pattern pattern, TokenType type) {
        if (pattern == null) {
            return;
        }

        Matcher m = pattern.matcher(source);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (start < 0 || end <= start || end > source.length()) {
                continue;
            }
            if (!isCovered(covered, start, end)) {
                markCovered(covered, start, end);
                tokens.add(new SyntaxToken(start, end - start, type));
            }
        }
    }

    private boolean isCovered(boolean[] covered, int start, int end) {
        for (int i = start; i < end; i++) {
            if (covered[i]) {
                return true;
            }
        }
        return false;
    }

    private void markCovered(boolean[] covered, int start, int end) {
        Arrays.fill(covered, start, end, true);
    }
}

