package com.kalynx.serverlessreviewtool.utils;

import com.kalynx.serverlessreviewtool.ui.mainpanels.LogsPanel;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * TeeOutputStream duplicates output to both the original stream and the LogsPanel.
 * Parses SLF4J log level from the output to determine correct severity.
 */
public class TeeOutputStream extends OutputStream {

    private final PrintStream original;
    private final LogsPanel logsPanel;
    private final StringBuilder lineBuffer = new StringBuilder();

    public TeeOutputStream(PrintStream original, LogsPanel logsPanel) {
        this.original = original;
        this.logsPanel = logsPanel;
    }

    @Override
    public void write(int b) {
        original.write(b);

        char c = (char) b;
        if (c == '\n') {
            flushLine();
        } else if (c != '\r') {
            lineBuffer.append(c);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        original.write(b, off, len);

        for (int i = off; i < off + len; i++) {
            char c = (char) b[i];
            if (c == '\n') {
                flushLine();
            } else if (c != '\r') {
                lineBuffer.append(c);
            }
        }
    }

    @Override
    public void flush() {
        original.flush();
    }

    private void flushLine() {
        if (lineBuffer.length() > 0 && logsPanel != null) {
            String line = lineBuffer.toString();

            // Parse SLF4J log level from format: [main] INFO class.name - message
            String level = extractLogLevel(line);

            switch (level) {
                case "ERROR":
                    logsPanel.appendError(line);
                    break;
                case "WARN":
                    logsPanel.appendWarn(line);
                    break;
                case "DEBUG":
                case "TRACE":
                    logsPanel.appendDebug(line);
                    break;
                case "INFO":
                default:
                    logsPanel.appendInfo(line);
                    break;
            }

            lineBuffer.setLength(0);
        }
    }

    private String extractLogLevel(String line) {
        // SLF4J simple format: [thread] LEVEL class.name - message
        // Look for common log levels in the line
        if (line.contains(" ERROR ")) {
            return "ERROR";
        } else if (line.contains(" WARN ")) {
            return "WARN";
        } else if (line.contains(" DEBUG ")) {
            return "DEBUG";
        } else if (line.contains(" TRACE ")) {
            return "TRACE";
        } else if (line.contains(" INFO ")) {
            return "INFO";
        }

        // Default to INFO for unrecognized format
        return "INFO";
    }
}


