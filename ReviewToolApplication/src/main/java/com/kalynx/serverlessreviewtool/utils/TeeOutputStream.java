package com.kalynx.serverlessreviewtool.utils;

import com.kalynx.serverlessreviewtool.ui.mainpanels.LogsPanel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TeeOutputStream duplicates output to both the original stream and the LogsPanel.
 * Parses SLF4J log level from the output to determine correct severity.
 */
public class TeeOutputStream extends OutputStream {

    private final PrintStream original;
    private volatile LogsPanel logsPanel;
    private final StringBuilder lineBuffer = new StringBuilder();
    private final Queue<BufferedLogLine> bufferedLines = new ConcurrentLinkedQueue<>();

    public TeeOutputStream(PrintStream original, LogsPanel logsPanel) {
        this.original = original;
        this.logsPanel = logsPanel;
    }

    /**
     * Attaches a logs panel target and flushes any buffered lines captured before UI initialization.
     *
     * @param logsPanel logs panel target
     */
    public void setLogsPanel(LogsPanel logsPanel) {
        this.logsPanel = logsPanel;
        flushBufferedLines();
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
        if (!lineBuffer.isEmpty()) {
            String line = lineBuffer.toString();

            String level = extractLogLevel(line);

            LogsPanel currentPanel = logsPanel;
            if (currentPanel == null) {
                bufferedLines.add(new BufferedLogLine(level, line));
            } else {
                appendToPanel(currentPanel, level, line);
            }

            lineBuffer.setLength(0);
        }
    }

    private void flushBufferedLines() {
        LogsPanel currentPanel = logsPanel;
        if (currentPanel == null) {
            return;
        }
        BufferedLogLine buffered;
        while ((buffered = bufferedLines.poll()) != null) {
            appendToPanel(currentPanel, buffered.level(), buffered.message());
        }
    }

    private void appendToPanel(LogsPanel panel, String level, String line) {
        switch (level) {
            case "ERROR":
                panel.appendError(line);
                break;
            case "WARN":
                panel.appendWarn(line);
                break;
            case "DEBUG":
            case "TRACE":
                panel.appendDebug(line);
                break;
            case "INFO":
            default:
                panel.appendInfo(line);
                break;
        }
    }

    private String extractLogLevel(String line) {
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

        return "INFO";
    }

    private record BufferedLogLine(String level, String message) {}
}


