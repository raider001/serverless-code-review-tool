package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.icons.CommentIcon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LineNumberedTextPane - A text pane with line numbers and line change indicators
 * Displays added/removed/modified line indicators with proper theming support
 */
public class LineNumberedTextPane extends JPanel {

    private final ThemeManager themeManager;
    private final JTextPane textPane;
    private final LineNumberPanel lineNumberPanel;

    // Synchronized font configuration
    private static final String FONT_NAME = "Consolas"; // Better looking code font
    private static final int FONT_SIZE_BASE = 14; // Increased to match UI font size
    private final int scaledFontSize;

    // Line type indicators
    private static final int LINE_CONTEXT = 0;
    private static final int LINE_ADDED = 1;
    private static final int LINE_REMOVED = 2;
    private static final int LINE_MODIFIED = 3;

    private final Set<Integer> addedLines = new HashSet<>();
    private final Set<Integer> removedLines = new HashSet<>();
    private final Set<Integer> modifiedLines = new HashSet<>();
    private final Map<Integer, List<String>> lineComments = new HashMap<>(); // line number -> list of comments

    public LineNumberedTextPane() {
        this.themeManager = ThemeManager.getInstance();
        this.textPane = new JTextPane();
        this.lineNumberPanel = new LineNumberPanel();
        this.scaledFontSize = themeManager.scale(FONT_SIZE_BASE);

        setLayout(new BorderLayout());

        // Add line number panel on the left
        add(lineNumberPanel, BorderLayout.WEST);

        // Add text pane in center
        add(textPane, BorderLayout.CENTER);

        // Listen to text changes to update line numbers
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lineNumberPanel.repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lineNumberPanel.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lineNumberPanel.repaint();
            }
        });

        // Add double-click listener for comments
        textPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    /**
     * Handle double-click to add a comment on a line
     */
    private void handleDoubleClick(MouseEvent e) {
        int offset = textPane.viewToModel2D(e.getPoint());
        String text = textPane.getText();

        // Find line number from offset
        int lineNumber = 1;
        for (int i = 0; i < offset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineNumber++;
            }
        }

        // Show comment popup
        final int finalLineNumber = lineNumber;
        CommentPopup popup = new CommentPopup(textPane, "");
        popup.setSubmitCallback(event -> {
            String comment = popup.getComment().trim();
            if (!comment.isEmpty()) {
                addComment(finalLineNumber, comment);
                lineNumberPanel.repaint();
            }
        });
        popup.setVisible(true);
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        textPane.setBackground(theme.getBackgroundColor());
        textPane.setForeground(theme.getForegroundColor());
        textPane.setFont(new Font(FONT_NAME, Font.PLAIN, scaledFontSize));
        textPane.setCaretColor(theme.getAccentColor());
        setBackground(theme.getBackgroundColor());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        applyTheme();
    }

    /**
     * Set text content
     */
    public void setText(String text) {
        textPane.setText(text);
        clearLineIndicators();
    }

    /**
     * Get text content
     */
    public String getText() {
        return textPane.getText();
    }

    /**
     * Mark a line as added
     */
    public void markLineAdded(int lineNumber) {
        addedLines.add(lineNumber);
        removedLines.remove(lineNumber);
        modifiedLines.remove(lineNumber);
        lineNumberPanel.repaint();
    }

    /**
     * Mark a line as removed
     */
    public void markLineRemoved(int lineNumber) {
        removedLines.add(lineNumber);
        addedLines.remove(lineNumber);
        modifiedLines.remove(lineNumber);
        lineNumberPanel.repaint();
    }

    /**
     * Mark a line as modified
     */
    public void markLineModified(int lineNumber) {
        modifiedLines.add(lineNumber);
        addedLines.remove(lineNumber);
        removedLines.remove(lineNumber);
        lineNumberPanel.repaint();
    }

    /**
     * Clear all line indicators
     */
    public void clearLineIndicators() {
        addedLines.clear();
        removedLines.clear();
        modifiedLines.clear();
        lineNumberPanel.repaint();
    }

    /**
     * Get the underlying text pane for styling
     */
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * Add a comment to a specific line
     */
    public void addComment(int lineNumber, String comment) {
        lineComments.computeIfAbsent(lineNumber, k -> new java.util.ArrayList<>()).add(comment);
    }

    /**
     * Get comments for a specific line
     */
    public List<String> getComments(int lineNumber) {
        return lineComments.getOrDefault(lineNumber, new java.util.ArrayList<>());
    }

    /**
     * Check if a line has comments
     */
    public boolean hasComments(int lineNumber) {
        return lineComments.containsKey(lineNumber) && !lineComments.get(lineNumber).isEmpty();
    }

    /**
     * Inner class for rendering line numbers with background color indicators
     */
    private class LineNumberPanel extends JPanel {
        private static final int RIGHT_MARGIN = 8;

        LineNumberPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = themeManager.getCurrentTheme();

            // Background
            g2d.setColor(theme.getBackgroundColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Border on right side
            g2d.setColor(theme.getBorderColor());
            g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

            // Use the SAME synchronized font as the text pane
            Font font = new Font(FONT_NAME, Font.PLAIN, scaledFontSize);
            FontMetrics fm = g2d.getFontMetrics(font);
            int lineHeight = fm.getHeight();
            int ascent = fm.getAscent();

            // Get text from pane
            String text = textPane.getText();
            int lineNumber = 1;
            int y = ascent + 2; // Align with text baseline

            // Draw line numbers and background colors
            String[] lines = text.split("\n", -1);
            for (String line : lines) {
                if (y - ascent > getHeight()) break;

                // Draw background color for line indicator
                drawLineBackground(g2d, lineNumber, y - ascent, lineHeight);

                // Draw line number with the same font as text pane
                g2d.setColor(theme.getForegroundColor());
                g2d.setFont(font);
                String lineStr = String.valueOf(lineNumber);
                int numberX = getWidth() - RIGHT_MARGIN - fm.stringWidth(lineStr);
                g2d.drawString(lineStr, numberX, y);

                y += lineHeight;
                lineNumber++;
            }

            // Update preferred width based on number of lines
            updatePreferredWidth(fm);
        }

        private void updatePreferredWidth(FontMetrics fm) {
            String text = textPane.getText();
            int lineCount = text.isEmpty() ? 1 : text.split("\n", -1).length;
            int maxLineWidth = fm.stringWidth(String.valueOf(lineCount));
            int preferredWidth = maxLineWidth + 20; // Add padding

            if (getPreferredSize().width != preferredWidth) {
                setPreferredSize(new Dimension(preferredWidth, 0));
                revalidate();
            }
        }

        private void drawLineBackground(Graphics2D g2d, int lineNumber, int y, int lineHeight) {
            Theme theme = themeManager.getCurrentTheme();

            if (addedLines.contains(lineNumber)) {
                g2d.setColor(theme.getAddedLineColor());
                g2d.fillRect(0, y, getWidth() - 1, lineHeight);
            } else if (removedLines.contains(lineNumber)) {
                g2d.setColor(theme.getRemovedLineColor());
                g2d.fillRect(0, y, getWidth() - 1, lineHeight);
            } else if (modifiedLines.contains(lineNumber)) {
                g2d.setColor(theme.getModifiedLineColor());
                g2d.fillRect(0, y, getWidth() - 1, lineHeight);
            }

            // Draw comment icon if comments exist
            if (hasComments(lineNumber)) {
                int commentCount = getComments(lineNumber).size();
                CommentIcon commentIcon = new CommentIcon(12, commentCount);
                commentIcon.paintIcon(null, g2d, RIGHT_MARGIN + 2, y + (lineHeight - 12) / 2);
            }
        }
    }
}


