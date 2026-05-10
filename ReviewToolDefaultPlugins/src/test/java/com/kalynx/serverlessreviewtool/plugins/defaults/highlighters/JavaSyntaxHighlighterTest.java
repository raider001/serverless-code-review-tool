package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSyntaxHighlighterTest {

    private final JavaSyntaxHighlighter highlighter = new JavaSyntaxHighlighter();

    @Test
    void tokenize_largeSourceWithNestedPatterns_doesNotOverflow() {
        String repeated = "class X { String s = \"\\\\\\\\\\\\\\\\\\\"; /* comment "
            + " with // and @Anno and >>>> and <<<= and escaped \\\\ */ int v = 12345; }\n";
        String source = repeated.repeat(2000);

        List<SyntaxHighlighterPlugin.SyntaxToken> tokens = assertDoesNotThrow(() -> highlighter.tokenize(source));

        assertTrue(tokens.size() > 1000);
    }

    @Test
    void tokenize_javaSnippet_returnsExpectedTokenTypes() {
        String source = "@Override\npublic class Demo {\n  // hi\n  String v = \"value\";\n}\n";

        List<SyntaxHighlighterPlugin.SyntaxToken> tokens = highlighter.tokenize(source);

        boolean hasAnnotation = tokens.stream().anyMatch(token -> token.type == SyntaxHighlighterPlugin.TokenType.ANNOTATION);
        boolean hasKeyword = tokens.stream().anyMatch(token -> token.type == SyntaxHighlighterPlugin.TokenType.KEYWORD);
        boolean hasComment = tokens.stream().anyMatch(token -> token.type == SyntaxHighlighterPlugin.TokenType.COMMENT);
        boolean hasString = tokens.stream().anyMatch(token -> token.type == SyntaxHighlighterPlugin.TokenType.STRING);

        assertTrue(hasAnnotation);
        assertTrue(hasKeyword);
        assertTrue(hasComment);
        assertTrue(hasString);
    }
}

