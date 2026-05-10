package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    public String getFileExtension() {
        return "java";
    }

    @Override
    public List<SyntaxToken> tokenize(String source) {
        List<SyntaxToken> tokens = new ArrayList<>();
        int length = source.length();
        int index = 0;

        while (index < length) {
            int commentLength = matchCommentLength(source, index);
            if (commentLength > 0) {
                tokens.add(new SyntaxToken(index, commentLength, TokenType.COMMENT));
                index += commentLength;
                continue;
            }

            int stringLength = matchStringLength(source, index);
            if (stringLength > 0) {
                tokens.add(new SyntaxToken(index, stringLength, TokenType.STRING));
                index += stringLength;
                continue;
            }

            int annotationLength = matchAnnotationLength(source, index);
            if (annotationLength > 0) {
                tokens.add(new SyntaxToken(index, annotationLength, TokenType.ANNOTATION));
                index += annotationLength;
                continue;
            }

            int numberLength = matchNumberLength(source, index);
            if (numberLength > 0) {
                tokens.add(new SyntaxToken(index, numberLength, TokenType.NUMBER));
                index += numberLength;
                continue;
            }

            int identifierLength = matchIdentifierLength(source, index);
            if (identifierLength > 0) {
                String identifier = source.substring(index, index + identifierLength);
                if (KEYWORDS.contains(identifier)) {
                    tokens.add(new SyntaxToken(index, identifierLength, TokenType.KEYWORD));
                } else if (isObjectReference(source, index + identifierLength)) {
                    tokens.add(new SyntaxToken(index, identifierLength, TokenType.OBJECT));
                } else {
                    tokens.add(new SyntaxToken(index, identifierLength, TokenType.VARIABLE));
                }
                index += identifierLength;
                continue;
            }

            int operatorLength = matchOperatorLength(source, index);
            if (operatorLength > 0) {
                tokens.add(new SyntaxToken(index, operatorLength, TokenType.OPERATOR));
                index += operatorLength;
                continue;
            }

            index++;
        }

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

    private int matchCommentLength(String source, int start) {
        int length = source.length();
        if (start + 1 >= length || source.charAt(start) != '/') {
            return 0;
        }

        char next = source.charAt(start + 1);
        if (next == '/') {
            int end = start + 2;
            while (end < length && source.charAt(end) != '\n') {
                end++;
            }
            return end - start;
        }
        if (next == '*') {
            int end = start + 2;
            while (end + 1 < length) {
                if (source.charAt(end) == '*' && source.charAt(end + 1) == '/') {
                    return end + 2 - start;
                }
                end++;
            }
            return length - start;
        }
        return 0;
    }

    private int matchStringLength(String source, int start) {
        int length = source.length();
        char quote = source.charAt(start);
        if (quote != '\'' && quote != '"') {
            return 0;
        }

        int index = start + 1;
        while (index < length) {
            char current = source.charAt(index);
            if (current == '\\') {
                index = Math.min(length, index + 2);
                continue;
            }
            if (current == quote) {
                return index + 1 - start;
            }
            index++;
        }
        return length - start;
    }

    private int matchAnnotationLength(String source, int start) {
        if (source.charAt(start) != '@') {
            return 0;
        }

        int identifierLength = matchIdentifierLength(source, start + 1);
        if (identifierLength <= 0) {
            return 0;
        }

        return identifierLength + 1;
    }

    private int matchNumberLength(String source, int start) {
        int length = source.length();
        if (!Character.isDigit(source.charAt(start))) {
            return 0;
        }

        int index = start;
        if (source.charAt(index) == '0' && index + 1 < length && (source.charAt(index + 1) == 'x' || source.charAt(index + 1) == 'X')) {
            index += 2;
            while (index < length && isHexDigit(source.charAt(index))) {
                index++;
            }
            return index - start;
        }

        while (index < length && Character.isDigit(source.charAt(index))) {
            index++;
        }

        if (index < length && source.charAt(index) == '.') {
            index++;
            while (index < length && Character.isDigit(source.charAt(index))) {
                index++;
            }
        }

        if (index < length && isNumberSuffix(source.charAt(index))) {
            index++;
        }

        return index - start;
    }

    private int matchIdentifierLength(String source, int start) {
        int length = source.length();
        if (start >= length || !isIdentifierStart(source.charAt(start))) {
            return 0;
        }

        int index = start + 1;
        while (index < length && isIdentifierPart(source.charAt(index))) {
            index++;
        }
        return index - start;
    }

    private int matchOperatorLength(String source, int start) {
        int length = source.length();
        if (start + 3 <= length) {
            String tri = source.substring(start, start + 3);
            if (tri.equals("<<=") || tri.equals(">>=")) {
                return 3;
            }
        }

        if (start + 2 <= length) {
            String bi = source.substring(start, start + 2);
            if (bi.equals("&&") || bi.equals("||") || bi.equals("->")
                || bi.equals("<<") || bi.equals(">>")
                || bi.equals("+=") || bi.equals("-=") || bi.equals("*=") || bi.equals("/=")
                || bi.equals("%=") || bi.equals("&=") || bi.equals("|=") || bi.equals("^=")
                || bi.equals("==") || bi.equals("!=") || bi.equals("<=") || bi.equals(">=")) {
                return 2;
            }
        }

        char current = source.charAt(start);
        if ("+-*/%&|^!=<>?:~".indexOf(current) >= 0) {
            return 1;
        }
        return 0;
    }

    private boolean isObjectReference(String source, int index) {
        int length = source.length();
        while (index < length && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return index < length && source.charAt(index) == '.';
    }

    private boolean isIdentifierStart(char value) {
        return value == '_' || value == '$' || Character.isLetter(value);
    }

    private boolean isIdentifierPart(char value) {
        return value == '_' || value == '$' || Character.isLetterOrDigit(value);
    }

    private boolean isHexDigit(char value) {
        return (value >= '0' && value <= '9')
            || (value >= 'a' && value <= 'f')
            || (value >= 'A' && value <= 'F');
    }

    private boolean isNumberSuffix(char value) {
        return value == 'f' || value == 'F' || value == 'l' || value == 'L' || value == 'd' || value == 'D';
    }
}



