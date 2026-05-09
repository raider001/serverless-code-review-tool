package com.kalynx.serverlessreviewtool.plugins.defaults.highlighters;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Dockerfile-style files.
 */
public class DockerfileSyntaxHighlighter extends RegexSyntaxHighlighter {

    @Override
    protected String extension() {
        return "dockerfile";
    }

    @Override
    protected Set<String> keywords() {
        return Set.of(
            "FROM", "RUN", "CMD", "LABEL", "EXPOSE", "ENV", "ADD", "COPY", "ENTRYPOINT", "VOLUME",
            "USER", "WORKDIR", "ARG", "ONBUILD", "STOPSIGNAL", "HEALTHCHECK", "SHELL", "AS"
        );
    }

    @Override
    protected Pattern commentPattern() {
        return Pattern.compile("#[^\\n]*");
    }

    @Override
    protected Pattern stringPattern() {
        return Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    }

    @Override
    protected Pattern numberPattern() {
        return Pattern.compile("\\b\\d+\\b");
    }

    @Override
    protected Pattern operatorPattern() {
        return Pattern.compile("\\|\\||&&|[=:\\[\\],]");
    }

    @Override
    protected Pattern variablePattern() {
        return Pattern.compile("\\$\\{?[A-Za-z_][A-Za-z0-9_]*\\}?");
    }
}

