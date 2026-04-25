package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.review.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * CommentPanel - Shows and allows adding inline comments for the review
 */
public class CommentPanel extends ThemedPanel {

    private final ThemeManager themeManager;
    private ReviewContext reviewContext;
    private ReviewFile currentFile;

    // UI Components
    private ThemedPanel commentsListPanel;
    private ThemedScrollPane scrollPane;
    private ThemedTextField lineNumberField;
    private ThemedTextArea commentTextArea;
    private ThemedButton addCommentButton;

    public CommentPanel(ReviewContext reviewContext) {
        this.themeManager = ThemeManager.getInstance();
        this.reviewContext = reviewContext;

        setLayout(new BorderLayout());
        setBorder(ThemedTitledBorder.create("Comments"));

        initializeComponents();
    }

    private void initializeComponents() {
        // Comments list at top
        commentsListPanel = new ThemedPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));

        scrollPane = new ThemedScrollPane(commentsListPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Add comment form at bottom
        ThemedPanel addCommentPanel = new ThemedPanel();
        addCommentPanel.setLayout(new BorderLayout(themeManager.scale(5), themeManager.scale(5)));
        addCommentPanel.setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(10),
            themeManager.scale(8),
            themeManager.scale(10),
            themeManager.scale(8)
        ));

        // Line number input
        ThemedPanel lineInputPanel = new ThemedPanel();
        lineInputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, themeManager.scale(5), 0));
        lineInputPanel.add(new ThemedLabel("Line:"));

        lineNumberField = new ThemedTextField(5);
        lineNumberField.setToolTipText("Line number for comment");
        lineInputPanel.add(lineNumberField);

        addCommentPanel.add(lineInputPanel, BorderLayout.NORTH);

        // Comment text area
        commentTextArea = new ThemedTextArea(3, 20);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        ThemedScrollPane textScrollPane = new ThemedScrollPane(commentTextArea);
        addCommentPanel.add(textScrollPane, BorderLayout.CENTER);

        // Add button
        addCommentButton = new ThemedButton("Add Comment");
        addCommentButton.addActionListener(e -> addComment());
        addCommentPanel.add(addCommentButton, BorderLayout.SOUTH);

        add(addCommentPanel, BorderLayout.SOUTH);

        // Show placeholder message
        showPlaceholder();
    }

    private void showPlaceholder() {
        commentsListPanel.removeAll();
        ThemedLabel placeholder = new ThemedLabel("No file selected");
        placeholder.setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(20),
            themeManager.scale(10),
            themeManager.scale(20),
            themeManager.scale(10)
        ));
        commentsListPanel.add(placeholder);
        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    public void loadCommentsForFile(ReviewFile file) {
        this.currentFile = file;
        commentsListPanel.removeAll();

        if (file == null) {
            showPlaceholder();
            return;
        }

        List<ReviewComment> comments = reviewContext.getCommentsForFile(file.getPath());

        if (comments.isEmpty()) {
            ThemedLabel noComments = new ThemedLabel("No comments yet");
            noComments.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(20),
                themeManager.scale(10),
                themeManager.scale(20),
                themeManager.scale(10)
            ));
            commentsListPanel.add(noComments);
        } else {
            for (ReviewComment comment : comments) {
                commentsListPanel.add(createCommentCard(comment));
                commentsListPanel.add(Box.createVerticalStrut(themeManager.scale(5)));
            }
        }

        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    private ThemedPanel createCommentCard(ReviewComment comment) {
        Theme theme = themeManager.getCurrentTheme();

        ThemedPanel card = new ThemedPanel();
        card.setLayout(new BorderLayout(themeManager.scale(5), themeManager.scale(5)));
        // Simple padding without line border for cleaner look
        card.setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(8),
            themeManager.scale(8),
            themeManager.scale(8),
            themeManager.scale(8)
        ));
        // Slightly different background to distinguish comments
        card.setBackground(theme.getInputBackground());

        // Header with author and line number
        ThemedPanel header = new ThemedPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT, themeManager.scale(5), 0));

        ThemedLabel authorLabel = new ThemedLabel(comment.getAuthor());
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(11)));
        authorLabel.setForeground(theme.getAccentColor());
        header.add(authorLabel);

        ThemedLabel lineLabel = new ThemedLabel("Line " + comment.getLineNumber());
        lineLabel.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
        header.add(lineLabel);

        ThemedLabel timeLabel = new ThemedLabel("• " + comment.getTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
        header.add(timeLabel);

        card.add(header, BorderLayout.NORTH);

        // Comment text
        ThemedTextArea textArea = new ThemedTextArea(comment.getText());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        card.add(textArea, BorderLayout.CENTER);

        return card;
    }

    private void addComment() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String lineText = lineNumberField.getText().trim();
        String commentText = commentTextArea.getText().trim();

        if (lineText.isEmpty() || commentText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both line number and comment", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int lineNumber = Integer.parseInt(lineText);

            // Create new comment
            String commentId = "COMMENT-" + System.currentTimeMillis();
            ReviewComment newComment = new ReviewComment(
                commentId,
                currentFile.getPath(),
                lineNumber,
                "Current User", // In real app, get from auth system
                commentText,
                "just now"
            );

            reviewContext.addComment(newComment);

            // Clear inputs
            lineNumberField.setText("");
            commentTextArea.setText("");

            // Reload comments
            loadCommentsForFile(currentFile);

            System.out.println("Added comment: " + newComment);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid line number", "Invalid Line Number", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        this.currentFile = null;
        showPlaceholder();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Recreate titled border with current theme colors
        // TitledBorders are immutable and must be recreated to pick up new theme colors
        setBorder(ThemedTitledBorder.create("Comments"));
        super.paintComponent(g);
    }
}

