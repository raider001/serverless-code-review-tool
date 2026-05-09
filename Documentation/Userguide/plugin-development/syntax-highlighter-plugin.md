# Syntax Highlighter Plugin

A `SyntaxHighlighterPlugin` provides token-based syntax highlighting for a single file type inside the code review diff viewer.

Each plugin handles exactly one file extension (e.g. `java`, `py`, `ts`). When a file is displayed, the application looks for the plugin registered for that extension and calls it to tokenize the source. The returned tokens are rendered as foreground-colour overlays on top of the existing diff background colours (added/removed/modified lines), so diff context is never obscured.

---

## Table of Contents

1. [How It Works](#how-it-works)
2. [Implementing the Plugin](#implementing-the-plugin)
3. [Token Types](#token-types)
4. [Example Implementation](#example-implementation)
5. [Colour Palette](#colour-palette)
6. [Registering Your Plugin](#registering-your-plugin)
7. [Tips and Best Practices](#tips-and-best-practices)

---

## How It Works

```
File selected in review viewer
         │
         ▼
Extract file extension (lower-case, no dot)
         │
         ▼
PluginManager.getSyntaxHighlighterFor(ext)
         │
    Plugin found?
   ┌──Yes────┐
   │         │
   ▼         No → no syntax highlighting
plugin.tokenize(source)
         │
         ▼
For each token: apply plugin.getColorForTokenType() as foreground
         │
         ▼
Render over existing diff background colours
```

Syntax highlighting is applied **after** diff background colours are painted. Each token's foreground colour is merged without disturbing the existing background, so removed/added/modified line colours remain visible.

---

## Implementing the Plugin

Each plugin handles exactly one file extension. Extend `SyntaxHighlighterPlugin` and implement three methods:

```java
package com.example.myplugin;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;
import java.awt.Color;
import java.util.List;

public class JavaSyntaxHighlighter extends SyntaxHighlighterPlugin {

    @Override
    public String getFileExtension() {
        return "java";  // Exactly one extension per plugin
    }

    @Override
    public List<SyntaxToken> tokenize(String source) {
        List<SyntaxToken> tokens = new java.util.ArrayList<>();
        // ... populate tokens from source ...
        return tokens;
    }

    @Override
    public Color getColorForTokenType(TokenType type) {
        return switch (type) {
            case KEYWORD    -> new Color(86, 156, 214);
            case STRING     -> new Color(206, 145, 120);
            // ... etc ...
            case DEFAULT    -> new Color(220, 220, 220);
        };
    }
}
```

### `getFileExtension()`

- Returns the **exact** file extension this plugin handles (lower-case, no dot).
- Only one plugin per extension is registered. Extensions are matched exactly.
- Example: `"java"`, `"py"`, `"ts"`, `"kt"`, `"go"`.

### `tokenize(String source)`

- `source` is the exact string loaded into the text pane (may include empty placeholder lines inserted by the diff alignment algorithm).
- Returns an ordered list of `SyntaxToken` objects. Tokens should **not overlap**.
- Tokens outside `[0, source.length())` are silently ignored.
- Return an empty list for files you cannot parse; the application falls back to plain text.

### `getColorForTokenType(TokenType type)`

- Returns the `java.awt.Color` to render for each token type.
- Your plugin defines the entire colour palette; each language can have its own look.

---

## Token Types

The `TokenType` enum lets you categorize different syntactic elements:

| `TokenType`  | Purpose                                          |
|-------------|--------------------------------------------------|
| `KEYWORD`   | Language keywords (`if`, `class`, `return` …)   |
| `STRING`    | String and character literals                    |
| `COMMENT`   | Line and block comments                          |
| `NUMBER`    | Integer, float, hex, and other numeric literals  |
| `TYPE`      | Class names, interfaces, and type references     |
| `ANNOTATION`| Annotations / decorators (`@Override`, `@Bean`)  |
| `OPERATOR`  | Operators and significant punctuation            |
| `DEFAULT`   | Everything else (fallback)                       |

---

## Example Implementation

A complete Java keyword highlighter using regex:

```java
package com.example.myplugin;

import com.kalynx.serverlessreviewtool.plugin.SyntaxHighlighterPlugin;
import java.awt.Color;
import java.util.*;
import java.util.regex.*;

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

    @Override
    public String getFileExtension() {
        return "java";
    }

    @Override
    public List<SyntaxToken> tokenize(String source) {
        List<SyntaxToken> tokens = new ArrayList<>();
        boolean[] covered = new boolean[source.length()];

        // Match patterns in priority order to avoid overlaps
        addTokens(tokens, covered, source, COMMENT_PATTERN,    TokenType.COMMENT);
        addTokens(tokens, covered, source, STRING_PATTERN,     TokenType.STRING);
        addTokens(tokens, covered, source, ANNOTATION_PATTERN, TokenType.ANNOTATION);
        addTokens(tokens, covered, source, NUMBER_PATTERN,     TokenType.NUMBER);
        addTokens(tokens, covered, source, KEYWORD_PATTERN,    TokenType.KEYWORD);

        return tokens;
    }

    @Override
    public Color getColorForTokenType(TokenType type) {
        return switch (type) {
            case KEYWORD    -> new Color(86, 156, 214);   // Blue
            case STRING     -> new Color(206, 145, 120);  // Orange
            case COMMENT    -> new Color(106, 153, 85);   // Green
            case NUMBER     -> new Color(181, 206, 168);  // Light green
            case TYPE       -> new Color(78, 201, 176);   // Teal
            case ANNOTATION -> new Color(220, 220, 170);  // Yellow
            case OPERATOR   -> new Color(180, 180, 180);  // Light grey
            case DEFAULT    -> new Color(220, 220, 220);  // Light grey
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
```

---

## Colour Palette

Your plugin controls its own colour palette via `getColorForTokenType()`. There is no "default theme" palette — each language plugin is responsible for choosing colours that work well.

**Recommendation:** Use standard IDE colour conventions so developers recognize tokens immediately. The built-in Java, Python, and TypeScript plugins use this palette:

| Token Type  | Colour       | RGB                  |
|------------|--------------|---------------------|
| KEYWORD    | Blue         | `(86, 156, 214)`     |
| STRING     | Orange       | `(206, 145, 120)`    |
| COMMENT    | Green        | `(106, 153, 85)`     |
| NUMBER     | Light green  | `(181, 206, 168)`    |
| TYPE       | Teal         | `(78, 201, 176)`     |
| ANNOTATION | Yellow       | `(220, 220, 170)`    |
| OPERATOR   | Light grey   | `(180, 180, 180)`    |
| DEFAULT    | Light grey   | `(220, 220, 220)`    |

This palette is optimized for **dark backgrounds**. Light-theme support may be added in future releases.

---

## Registering Your Plugin

Add your class to `META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin`:

```
com.example.myplugin.JavaSyntaxHighlighter
com.example.myplugin.PythonSyntaxHighlighter
com.example.myplugin.TypeScriptSyntaxHighlighter
```

You can register multiple language plugins in a single JAR. Each must handle exactly one extension.

---

## Tips and Best Practices

- **One extension per plugin instance.** Don't try to handle multiple extensions in a single class; create separate classes for each language.
- **Avoid overlapping tokens.** Use the `covered[]` array technique from the example to prevent two patterns from claiming the same character positions.
- **Handle empty strings gracefully.** The diff viewer may pass empty placeholder lines; return an empty list rather than throwing.
- **Do not perform I/O in `tokenize()`.** It is called on the Swing Event Dispatch Thread. Precompile patterns as static constants and keep the method allocation-light.
- **Test against large files.** Profile your regex patterns on realistic source files (1000+ lines) to avoid performance issues.
- **Consider operator precedence.** Match longer operators before shorter ones (e.g. `===` before `==`).
- **Precompile your patterns.** Store `Pattern` objects as `private static final` to avoid repeated recompilation.

