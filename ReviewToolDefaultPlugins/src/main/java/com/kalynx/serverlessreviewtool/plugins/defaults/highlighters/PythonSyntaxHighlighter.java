package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;

import java.awt.Color;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Python source files.
 * Highlights keywords, strings, comments, numbers, type hints, and operators.
 */
public class PythonSyntaxHighlighter extends SyntaxHighlighterPlugin {

    private static final Set<String> KEYWORDS = Set.of(
        "False", "None", "True", "and", "as", "assert", "async", "await",
        "break", "class", "continue", "def", "del", "elif", "else", "except",
        "finally", "for", "from", "global", "if", "import", "in", "is",
        "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try",
        "while", "with", "yield"
    );

    private static final Pattern KEYWORD_PATTERN =
        Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");
    private static final Pattern STRING_PATTERN =
        Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Pattern COMMENT_PATTERN =
        Pattern.compile("#[^\n]*");
    private static final Pattern NUMBER_PATTERN =
        Pattern.compile("\\b(0x[0-9A-Fa-f]+|0o[0-7]+|0b[01]+|\\d+\\.?\\d*([eE][+-]?\\d+)?)\\b");
    private static final Pattern OPERATOR_PATTERN =
        Pattern.compile("[+\\-*/%@&|^!=<>?:]|\\*\\*|//|<<|>>|->|\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<=|>>=|<=|>=|==|!=");

    @Override
    public String getFileExtension() {
        return "py";
    }

    @Override
    public List<SyntaxToken> tokenize(String source) {
        List<SyntaxToken> tokens = new ArrayList<>();
        boolean[] covered = new boolean[source.length()];

        addTokens(tokens, covered, source, COMMENT_PATTERN,   TokenType.COMMENT);
        addTokens(tokens, covered, source, STRING_PATTERN,    TokenType.STRING);
        addTokens(tokens, covered, source, NUMBER_PATTERN,    TokenType.NUMBER);
        addTokens(tokens, covered, source, KEYWORD_PATTERN,   TokenType.KEYWORD);
        addTokens(tokens, covered, source, OPERATOR_PATTERN,  TokenType.OPERATOR);

        return tokens;
    }

    @Override
    public Color getColorForTokenType(TokenType type) {
        return switch (type) {
            case KEYWORD    -> new Color(86, 156, 214);      // Blue
            case STRING     -> new Color(206, 145, 120);     // Orange
            case COMMENT    -> new Color(106, 153, 85);      // Green
            case NUMBER     -> new Color(181, 206, 168);     // Light green
            case TYPE       -> new Color(78, 201, 176);      // Teal
            case ANNOTATION -> new Color(220, 220, 170);     // Yellow
            case OPERATOR   -> new Color(180, 180, 180);     // Light grey
            case DEFAULT    -> new Color(220, 220, 220);     // Light grey
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

