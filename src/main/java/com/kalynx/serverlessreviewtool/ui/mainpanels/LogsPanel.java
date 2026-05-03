package com.kalynx.serverlessreviewtool.ui.mainpanels;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LogsPanel displays application logs in real-time with color-coded severity levels.
 * Automatically scrolls to show the latest log entries.
 */
public class LogsPanel extends ThemedPanel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int MAX_LOG_LINES = 10000;

    private final JTextPane logTextPane;
    private final StyledDocument document;
    private final ThemedScrollPane scrollPane;
    private final ThemedButton clearButton;
    private final ThemeManager themeManager;

    private Style infoStyle;
    private Style warnStyle;
    private Style errorStyle;
    private Style debugStyle;
    private Style timestampStyle;

    public LogsPanel() {
        this.themeManager = ThemeManager.getInstance();
        this.logTextPane = new JTextPane();
        this.document = logTextPane.getStyledDocument();
        this.scrollPane = new ThemedScrollPane(logTextPane);
        this.clearButton = new ThemedButton("Clear Logs");

        configureLogPane();
        initializeStyles();
        configureLayout();
        setupListeners();
    }

    private void configureLogPane() {
        logTextPane.setEditable(false);
        logTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        updateLogPaneColors();
    }

    private void updateLogPaneColors() {
        logTextPane.setBackground(themeManager.getCurrentTheme().getBackgroundColor());
        logTextPane.setForeground(themeManager.getCurrentTheme().getForegroundColor());
        logTextPane.setCaretColor(themeManager.getCurrentTheme().getForegroundColor());
    }

    private void initializeStyles() {
        timestampStyle = document.addStyle("timestamp", null);
        StyleConstants.setForeground(timestampStyle, new Color(128, 128, 128));
        StyleConstants.setBold(timestampStyle, false);

        infoStyle = document.addStyle("info", null);
        StyleConstants.setForeground(infoStyle, new Color(70, 130, 180));
        StyleConstants.setBold(infoStyle, false);

        warnStyle = document.addStyle("warn", null);
        StyleConstants.setForeground(warnStyle, new Color(255, 165, 0));
        StyleConstants.setBold(warnStyle, true);

        errorStyle = document.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, new Color(220, 50, 47));
        StyleConstants.setBold(errorStyle, true);

        debugStyle = document.addStyle("debug", null);
        StyleConstants.setForeground(debugStyle, new Color(150, 150, 150));
        StyleConstants.setBold(debugStyle, false);
    }

    private void configureLayout() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));

        ThemedPanel headerPanel = new ThemedPanel();
        headerPanel.setLayout(new MigLayout("insets 5", "[grow][]", "[]"));
        headerPanel.setBorder(ThemedTitledBorder.create("Application Logs"));

        headerPanel.add(clearButton, "align right");

        add(headerPanel, "growx, wrap");
        add(scrollPane, "grow");
    }

    private void setupListeners() {
        clearButton.addActionListener(e -> clearLogs());
    }

    /**
     * Appends a log message with INFO level.
     *
     * @param message the log message
     */
    public void appendInfo(String message) {
        appendLog("INFO", message, infoStyle);
    }

    /**
     * Appends a log message with WARN level.
     *
     * @param message the log message
     */
    public void appendWarn(String message) {
        appendLog("WARN", message, warnStyle);
    }

    /**
     * Appends a log message with ERROR level.
     *
     * @param message the log message
     */
    public void appendError(String message) {
        appendLog("ERROR", message, errorStyle);
    }

    /**
     * Appends a log message with DEBUG level.
     *
     * @param message the log message
     */
    public void appendDebug(String message) {
        appendLog("DEBUG", message, debugStyle);
    }

    private void appendLog(String level, String message, Style levelStyle) {
        SwingUtilities.invokeLater(() -> {
            try {
                String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

                document.insertString(document.getLength(), timestamp + " ", timestampStyle);
                document.insertString(document.getLength(), String.format("%-5s", level) + " ", levelStyle);
                document.insertString(document.getLength(), message + "\n", null);

                trimLogIfNeeded();
                scrollToBottom();

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void trimLogIfNeeded() {
        try {
            Element root = document.getDefaultRootElement();
            int lineCount = root.getElementCount();

            if (lineCount > MAX_LOG_LINES) {
                int linesToRemove = lineCount - MAX_LOG_LINES;
                Element lineToRemove = root.getElement(linesToRemove - 1);
                int endOffset = lineToRemove.getEndOffset();
                document.remove(0, endOffset);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void scrollToBottom() {
        logTextPane.setCaretPosition(document.getLength());
    }

    private void clearLogs() {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}


