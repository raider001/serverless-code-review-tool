package com.kalynx.serverlessreviewtool.ui.review;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.LineNumberedTextPane;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedScrollPane;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedSplitPane;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.CodeViewerModel;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * DiffViewerPanel - Shows file diffs in side-by-side or unified mode
 */
public class DiffViewerPanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager = ThemeManager.getInstance();
    private transient final CodeViewerModel codeViewerModel;

    private DiffViewMode currentMode = DiffViewMode.SIDE_BY_SIDE;
    private ThemedPanel contentPanel;

    private LineNumberedTextPane leftPane;
    private LineNumberedTextPane rightPane;
    private LineNumberedTextPane unifiedPane;

    private transient ReviewFile currentFile;
    private transient Commit startCommit;
    private transient Commit endCommit;
    private transient Theme lastRenderedTheme;

    public DiffViewerPanel(CodeViewerModel codeViewerModel) {
        this.codeViewerModel = codeViewerModel;
        setLayout(new BorderLayout());
        initializeComponents();
        setupModelListeners();
    }

    private void initializeComponents() {

        contentPanel = new ThemedPanel(new CardLayout());

        // Create side-by-side view
        createSideBySideView();

        // Create unified view
        createUnifiedView();

        add(contentPanel, BorderLayout.CENTER);

        // Show initial view
        switchViewMode();
    }

    private void setupModelListeners() {
        codeViewerModel.leftContent.addChangeListener(content -> updateLeftContent());
        codeViewerModel.rightContent.addChangeListener(content -> updateRightContent());
        codeViewerModel.unifiedDiffContent.addChangeListener(content -> updateUnifiedContent());
        codeViewerModel.diffMode.addChangeListener(mode -> {
            if (mode == CodeViewerModel.DiffMode.SIDE_BY_SIDE) {
                setViewMode(DiffViewMode.SIDE_BY_SIDE);
            } else {
                setViewMode(DiffViewMode.UNIFIED);
            }
        });
        codeViewerModel.selectedFile.addChangeListener(file -> {
            this.currentFile = file;
            this.startCommit = codeViewerModel.startCommit.getValue();
            this.endCommit = codeViewerModel.endCommit.getValue();
        });
    }

    private void updateLeftContent() {
        String content = codeViewerModel.leftContent.getValue();
        if (content != null && leftPane != null) {
            if (content.isEmpty()) {
                System.err.println("[DiffViewerPanel] WARNING: Left content is EMPTY");
            } else {
                System.out.println("[DiffViewerPanel] Setting left content: " + content.length() + " chars");
            }
            SwingUtilities.invokeLater(() -> leftPane.setText(content));
        }
    }

    private void updateRightContent() {
        String content = codeViewerModel.rightContent.getValue();
        if (content != null && rightPane != null) {
            if (content.isEmpty()) {
                System.err.println("[DiffViewerPanel] WARNING: Right content is EMPTY");
            } else {
                System.out.println("[DiffViewerPanel] Setting right content: " + content.length() + " chars");
            }
            SwingUtilities.invokeLater(() -> rightPane.setText(content));
        }
    }

    private void updateUnifiedContent() {
        String content = codeViewerModel.unifiedDiffContent.getValue();
        if (content != null && unifiedPane != null) {
            if (content.isEmpty()) {
                System.err.println("[DiffViewerPanel] WARNING: Unified content is EMPTY");
            } else {
                System.out.println("[DiffViewerPanel] Setting unified content: " + content.length() + " chars");
            }
            SwingUtilities.invokeLater(() -> unifiedPane.setText(content));
        }
    }

    private void createSideBySideView() {
        // Create line numbered panes for both sides
        leftPane = new LineNumberedTextPane();
        rightPane = new LineNumberedTextPane();

        // Wrap in scroll panes
        ThemedScrollPane leftScrollPane = new ThemedScrollPane(leftPane);
        leftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        ThemedScrollPane rightScrollPane = new ThemedScrollPane(rightPane);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Synchronize scrolling between left and right panes
        synchronizeScrollPanes(leftScrollPane, rightScrollPane);

        // Create split pane with the two scrolled line-numbered panes
        ThemedSplitPane splitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setResizeWeight(0.5);

        contentPanel.add(splitPane, DiffViewMode.SIDE_BY_SIDE.name());
    }

    private void createUnifiedView() {
        unifiedPane = new LineNumberedTextPane();

        // Wrap in scroll pane
        ThemedScrollPane unifiedScrollPane = new ThemedScrollPane(unifiedPane);
        unifiedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        unifiedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        contentPanel.add(unifiedScrollPane, DiffViewMode.UNIFIED.name());
    }

    private void synchronizeScrollPanes(ThemedScrollPane leftPane, ThemedScrollPane rightPane) {
        // Link vertical scroll bars
        JScrollBar leftScrollBar = leftPane.getVerticalScrollBar();
        JScrollBar rightScrollBar = rightPane.getVerticalScrollBar();

        if (leftScrollBar != null && rightScrollBar != null) {
            leftScrollBar.addAdjustmentListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    rightScrollBar.setValue(e.getValue());
                }
            });

            rightScrollBar.addAdjustmentListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    leftScrollBar.setValue(e.getValue());
                }
            });
        }
    }

    private void switchViewMode() {
        CardLayout layout = (CardLayout) contentPanel.getLayout();
        layout.show(contentPanel, currentMode.name());

        if (currentFile != null) {
            showDiff(currentFile, startCommit, endCommit);
        }
    }

    public void setViewMode(DiffViewMode mode) {
        if (mode != null && mode != currentMode) {
            currentMode = mode;
            switchViewMode();
        }
    }

    public void showDiff(ReviewFile file, Commit newStartCommit, Commit newEndCommit) {
        this.currentFile = file;
        this.startCommit = newStartCommit;
        this.endCommit = newEndCommit;
        this.lastRenderedTheme = themeManager.getCurrentTheme();

        if (currentMode == DiffViewMode.SIDE_BY_SIDE) {
            showSideBySideDiff(file, newStartCommit, newEndCommit);
        } else {
            showUnifiedDiff(file, newStartCommit, newEndCommit);
        }
    }

    private void showSideBySideDiff(ReviewFile file, Commit startCommit, Commit endCommit) {
        // Get content from model
        String beforeContent = codeViewerModel.leftContent.getValue();
        String afterContent = codeViewerModel.rightContent.getValue();

        // Fall back to emptyif not available
        if (beforeContent == null || beforeContent.isEmpty()) {
            beforeContent = "// Content not available for " + file.getPath();
        }
        if (afterContent == null || afterContent.isEmpty()) {
            afterContent = "// Content not available for " + file.getPath();
        }

        // Set content and apply highlighting
        highlightDiffWithInlineChanges(leftPane, rightPane, beforeContent, afterContent);
    }

    private void showUnifiedDiff(ReviewFile file, Commit startCommit, Commit endCommit) {
        // Get unified diff from model
        String unifiedDiff = codeViewerModel.unifiedDiffContent.getValue();

        // Fall back to placeholder if not available
        if (unifiedDiff == null || unifiedDiff.isEmpty()) {
            unifiedDiff = "// Unified diff not available for " + file.getPath();
        }

        highlightUnifiedDiff(unifiedPane, unifiedDiff);
    }

    private void highlightDiffWithInlineChanges(LineNumberedTextPane leftPane, LineNumberedTextPane rightPane,
                                                 String beforeContent, String afterContent) {
        // Set text in both panes
        leftPane.setText(beforeContent);
        rightPane.setText(afterContent);

        Theme theme = themeManager.getCurrentTheme();
        JTextPane leftTextPane = leftPane.getTextPane();
        JTextPane rightTextPane = rightPane.getTextPane();

        StyledDocument leftDoc = leftTextPane.getStyledDocument();
        StyledDocument rightDoc = rightTextPane.getStyledDocument();

        // Create styles - set both foreground (theme) and background (highlight)
        // because setCharacterAttributes with replace=true wipes all attributes
        Style removedStyle = leftTextPane.addStyle("removed", null);
        StyleConstants.setForeground(removedStyle, theme.getForegroundColor());
        StyleConstants.setBackground(removedStyle, theme.getRemovedLineColor());

        Style addedStyle = rightTextPane.addStyle("added", null);
        StyleConstants.setForeground(addedStyle, theme.getForegroundColor());
        StyleConstants.setBackground(addedStyle, theme.getAddedLineColor());

        Style defaultStyle = leftTextPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, theme.getForegroundColor());

        Style defaultStyleRight = rightTextPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyleRight, theme.getForegroundColor());

        // Split into lines and compare
        String[] beforeLines = beforeContent.split("\n", -1);
        String[] afterLines = afterContent.split("\n", -1);

        int beforeOffset = 0;
        int afterOffset = 0;

        int maxLines = Math.max(beforeLines.length, afterLines.length);
        for (int i = 0; i < maxLines; i++) {
            String beforeLine = i < beforeLines.length ? beforeLines[i] : "";
            String afterLine = i < afterLines.length ? afterLines[i] : "";

            int beforeLineLength = beforeLine.length() + 1;
            int afterLineLength = afterLine.length() + 1;

            // Apply default style first
            if (beforeOffset + beforeLineLength <= leftDoc.getLength()) {
                leftDoc.setCharacterAttributes(beforeOffset, beforeLineLength, defaultStyle, true);
            }
            if (afterOffset + afterLineLength <= rightDoc.getLength()) {
                rightDoc.setCharacterAttributes(afterOffset, afterLineLength, defaultStyleRight, true);
            }

            // Find and highlight differences within the line
            if (!beforeLine.equals(afterLine)) {
                // Find common prefix
                int commonPrefix = 0;
                int minLen = Math.min(beforeLine.length(), afterLine.length());
                while (commonPrefix < minLen && beforeLine.charAt(commonPrefix) == afterLine.charAt(commonPrefix)) {
                    commonPrefix++;
                }

                // Find common suffix
                int commonSuffix = 0;
                while (commonSuffix < minLen - commonPrefix &&
                       beforeLine.charAt(beforeLine.length() - 1 - commonSuffix) ==
                       afterLine.charAt(afterLine.length() - 1 - commonSuffix)) {
                    commonSuffix++;
                }

                // Highlight the differences
                if (commonPrefix < beforeLine.length() - commonSuffix) {
                    int diffLength = beforeLine.length() - commonPrefix - commonSuffix;
                    leftDoc.setCharacterAttributes(beforeOffset + commonPrefix, diffLength, removedStyle, true);
                }

                if (commonPrefix < afterLine.length() - commonSuffix) {
                    int diffLength = afterLine.length() - commonPrefix - commonSuffix;
                    rightDoc.setCharacterAttributes(afterOffset + commonPrefix, diffLength, addedStyle, true);
                }
            }

            beforeOffset += beforeLineLength;
            afterOffset += afterLineLength;
        }
    }

    private void highlightUnifiedDiff(LineNumberedTextPane pane, String unifiedDiff) {
        pane.setText(unifiedDiff);

        Theme theme = themeManager.getCurrentTheme();
        JTextPane textPane = pane.getTextPane();
        StyledDocument doc = textPane.getStyledDocument();

        // Create styles - set both foreground (theme) and background (highlight)
        // because setCharacterAttributes with replace=true wipes all attributes
        Style addedStyle = textPane.addStyle("added", null);
        StyleConstants.setForeground(addedStyle, theme.getForegroundColor());
        StyleConstants.setBackground(addedStyle, theme.getAddedLineColor());

        Style removedStyle = textPane.addStyle("removed", null);
        StyleConstants.setForeground(removedStyle, theme.getForegroundColor());
        StyleConstants.setBackground(removedStyle, theme.getRemovedLineColor());

        Style defaultStyle = textPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, theme.getForegroundColor());

        // Apply styles based on line prefixes
        String[] lines = unifiedDiff.split("\n");
        int offset = 0;
        int lineNumber = 1;
        for (String line : lines) {
            int lineLength = line.length() + 1; // +1 for newline

            if (line.startsWith("+") && !line.startsWith("+++")) {
                doc.setCharacterAttributes(offset, lineLength, addedStyle, true);
                pane.markLineAdded(lineNumber);
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                doc.setCharacterAttributes(offset, lineLength, removedStyle, true);
                pane.markLineRemoved(lineNumber);
            } else {
                doc.setCharacterAttributes(offset, lineLength, defaultStyle, true);
            }

            offset += lineLength;
            lineNumber++;
        }
    }

    public void clear() {
        leftPane.setText("");
        rightPane.setText("");
        unifiedPane.setText("");
        currentFile = null;
    }


    public void setOnLineDoubleClickListener(java.util.function.Consumer<Integer> listener) {
        leftPane.setOnLineDoubleClickListener(listener);
        rightPane.setOnLineDoubleClickListener(listener);
        unifiedPane.setOnLineDoubleClickListener(listener);
    }

    public void setCommentsForCurrentFile(java.util.List<ReviewComment> comments) {
        leftPane.setComments(comments);
        rightPane.setComments(comments);
        unifiedPane.setComments(comments);
    }

    @Override
    public void paint(Graphics g) {
        if (currentFile != null && lastRenderedTheme != themeManager.getCurrentTheme()) {
            showDiff(currentFile, startCommit, endCommit);
        }
        super.paint(g);
    }
}

