package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;

import java.awt.Color;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Java source files.
 * Highlights keywords, strings, comments, numbers, types, annotations, and operators.
 */
public class JavaSyntaxHighlighter extends SyntaxHighlighterPlugin {

    private static final Set<String> KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "var", "void", "volatile", "while", "record", "sealed",
        "permits", "yield", "null", "true", "false"
    );

    private static final Pattern KEYWORD_PATTERN =
        Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");
    private static final Pattern STRING_PATTERN =
        Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Pattern COMMENT_PATTERN =
        Pattern.compile("//[^\n]*|/\\*[\\s\\S]*?\\*/");
    private static final Pattern NUMBER_PATTERN =
        Pattern.compile("\\b(0x[0-9A-Fa-f]+|\\d+\\.?\\d*[fFlLdD]?)\\b");
    private static final Pattern ANNOTATION_PATTERN =
        Pattern.compile("@\\w+");
    private static final Pattern OPERATOR_PATTERN =
        Pattern.compile("[+\\-*/%&|^!=<>?:~]|&&|\\|\\||->|<<|>>|\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<=|>>=");
    private static final Pattern OBJECT_PATTERN =
        Pattern.compile("\\b[_$a-zA-Z][_$a-zA-Z0-9]*\\b(?=\\s*\\.)");
    private static final Pattern VARIABLE_PATTERN =
        Pattern.compile("\\b[_a-z][_a-zA-Z0-9]*\\b");

    @Override
    public String getFileExtension() {
        return "java";
    }

    @Override
    public List<SyntaxToken> tokenize(String source) {
        List<SyntaxToken> tokens = new ArrayList<>();
        boolean[] covered = new boolean[source.length()];

        addTokens(tokens, covered, source, COMMENT_PATTERN,    TokenType.COMMENT);
        addTokens(tokens, covered, source, STRING_PATTERN,     TokenType.STRING);
        addTokens(tokens, covered, source, ANNOTATION_PATTERN, TokenType.ANNOTATION);
        addTokens(tokens, covered, source, NUMBER_PATTERN,     TokenType.NUMBER);
        addTokens(tokens, covered, source, KEYWORD_PATTERN,    TokenType.KEYWORD);
        addTokens(tokens, covered, source, OPERATOR_PATTERN,   TokenType.OPERATOR);
        addTokens(tokens, covered, source, OBJECT_PATTERN,     TokenType.OBJECT);
        addTokens(tokens, covered, source, VARIABLE_PATTERN,   TokenType.VARIABLE);

        return tokens;
    }

    @Override
    public Color getColorForTokenType(TokenType type, boolean darkTheme) {
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

    private void addTokens(List<SyntaxToken> tokens, boolean[] covered,
                           String source, Pattern pattern, TokenType type) {
        Matcher m = pattern.matcher(source);
        while (m.find()) {
            int start = m.start();
            int end   = m.end();
            if (!isCovered(covered, start, end)) {
                markCovered(covered, start, end);
                tokens.add(new SyntaxToken(start, end - start, type));
            }
        }
    }

    private boolean isCovered(boolean[] covered, int start, int end) {
        for (int i = start; i < end; i++) {
            if (covered[i]) return true;
        }
        return false;
    }

    private void markCovered(boolean[] covered, int start, int end) {
        Arrays.fill(covered, start, end, true);
    }
}



