package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for SQL files.
 */
public class SqlSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "sql";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "select", "from", "where", "join", "left", "right", "inner", "outer", "on", "group", "by",
            "order", "having", "insert", "into", "values", "update", "set", "delete", "create", "table",
            "alter", "drop", "index", "view", "distinct", "union", "all", "limit", "offset", "and", "or",
            "not", "null", "is", "in", "exists", "like", "between", "case", "when", "then", "else", "end",
            "as", "primary", "key", "foreign", "references", "constraint", "default", "true", "false"
        );
    }

    @Override
    protected boolean keywordCaseInsensitive() {
        return true;
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("--[^\n]*|/\\*[\\s\\S]*?\\*/");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("'([^'\\\\]|\\\\.)*'|\"([^\"\\\\]|\\\\.)*\"");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");
    }

    @Override
    protected Pattern typePattern() {
        return Pattern.compile("(?i)\\b(varchar|char|text|int|integer|bigint|smallint|numeric|decimal|float|double|date|timestamp|boolean|json|uuid)\\b");
    }

    @Override
    protected Pattern objectPattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b(?=\\s*\\.)");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("<>|!=|<=|>=|[=+\\-*/%<>(){},.;]");
    }
}

