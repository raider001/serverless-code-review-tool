package com.kalynx.serverlessreviewtool.plugin;

import java.awt.Color;
import java.util.List;

/**
 * Base class for syntax highlighting plugins.
 * Each plugin handles exactly one file extension and provides token-based syntax
 * highlighting. The plugin defines both the tokenization logic and the colour palette
 * for each token type.
 *
 * <p>Implementations return a flat list of {@link SyntaxToken}s that describe coloured
 * regions within a source string. Each token is rendered as a foreground-colour overlay
 * on top of the existing diff background colours, so diff context is never obscured.
 */
public abstract class SyntaxHighlighterPlugin implements Plugin {

    @Override
    public void initialize() {}

    /**
     * Returns the file extension this plugin handles (without leading dot, lower-case).
     * Each plugin handles exactly one extension: {@code "java"}, {@code "py"}, {@code "ts"}, etc.
     *
     * @return lower-case file extension without dot
     */
    public abstract String getFileExtension();

    /**
     * Tokenizes the supplied source code and returns a list of typed regions.
     *
     * <p>Each {@link SyntaxToken} carries a character {@code offset} and {@code length}
     * measured against the {@code source} string passed to this method. Tokens must not
     * overlap; behaviour is undefined when they do. Tokens outside the bounds of
     * {@code source} are silently ignored by the application.
     *
     * @param source the full source code to tokenize
     * @return ordered list of syntax tokens; never {@code null}
     */
    public abstract List<SyntaxToken> tokenize(String source);

    /**
     * Returns the foreground colour to use when rendering the given token type.
     * This allows each plugin to define readable colours for both dark and light themes.
     *
     * @param type semantic token type
     * @param darkTheme true when the active theme is dark
     * @return colour for this token type; never {@code null}
     */
    public abstract Color getColorForTokenType(TokenType type, boolean darkTheme);

    /**
     * A typed, positioned region within a source string.
     */
    public static final class SyntaxToken {

        /** Zero-based start position within the source string. */
        public final int offset;

        /** Number of characters covered by this token. */
        public final int length;

        /** Semantic category used to select a display colour. */
        public final TokenType type;

        /**
         * Creates a new syntax token.
         *
         * @param offset zero-based start position
         * @param length number of characters
         * @param type   token category
         */
        public SyntaxToken(int offset, int length, TokenType type) {
            this.offset = offset;
            this.length = length;
            this.type = type;
        }
    }

    /**
     * Semantic categories for syntax tokens.
     * Each plugin defines its own colour mapping via {@link #getColorForTokenType(TokenType, boolean)}.
     */
    public enum TokenType {
        /** Language keywords (e.g. {@code if}, {@code class}, {@code return}). */
        KEYWORD,
        /** String and character literals. */
        STRING,
        /** Line and block comments. */
        COMMENT,
        /** Numeric literals (integer, float, hex, etc.). */
        NUMBER,
        /** Type names, class names, and interfaces. */
        TYPE,
        /** Annotations or decorators (e.g. {@code @Override}). */
        ANNOTATION,
        /** Operators and punctuation with semantic meaning. */
        OPERATOR,
        /** Object identifiers, typically instance references before member access. */
        OBJECT,
        /** Variable and identifier names. */
        VARIABLE,
        /** All other text; rendered with the default foreground colour. */
        DEFAULT
    }
}


