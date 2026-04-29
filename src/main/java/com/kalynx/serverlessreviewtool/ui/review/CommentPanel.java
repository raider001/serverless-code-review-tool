package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * CommentPanel - Shows and allows adding inline comments with threading and resolution tracking
 */
public class CommentPanel extends ThemedPanel {

    private final ThemeManager themeManager = ThemeManager.getInstance();
    private ReviewContext reviewContext;
    private ReviewFile currentFile;

    private ThemedPanel commentsListPanel;
    private ThemedTextField lineNumberField;
    private ThemedTextArea commentTextArea;
    private ThemedCheckBox needsResolutionCheckBox;
    private ThemedButton addCommentButton;

    private ReviewComment replyToComment;

    public CommentPanel(ReviewContext reviewContext) {
        this.reviewContext = reviewContext;
        setBorder(ThemedTitledBorder.create("Comments"));

        configureLayout();
        setupListeners();
        showPlaceholder();
    }

    private void configureLayout() {
        setLayout(new BorderLayout());

        commentsListPanel = new ThemedPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));

        ThemedScrollPane scrollPane = new ThemedScrollPane(commentsListPanel);
        add(scrollPane, BorderLayout.CENTER);

        ThemedPanel addCommentPanel = createAddCommentPanel();
        add(addCommentPanel, BorderLayout.SOUTH);
    }

    private ThemedPanel createAddCommentPanel() {
        ThemedPanel panel = new ThemedPanel(new MigLayout("fill, insets 10", "[grow]", "[]5[]5[]"));

        ThemedPanel linePanel = new ThemedPanel(new MigLayout("insets 0", "[]5[]5[grow]", "[]"));
        linePanel.add(new ThemedLabel("Line:"));

        lineNumberField = new ThemedTextField(5);
        lineNumberField.setToolTipText("Line number for comment");
        linePanel.add(lineNumberField);

        needsResolutionCheckBox = new ThemedCheckBox("Needs Resolution", false);
        linePanel.add(needsResolutionCheckBox, "growx");

        panel.add(linePanel, "growx, wrap");

        commentTextArea = new ThemedTextArea(3, 20);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        ThemedScrollPane textScrollPane = new ThemedScrollPane(commentTextArea);
        panel.add(textScrollPane, "grow, wrap");

        addCommentButton = new ThemedButton("Add Comment");
        panel.add(addCommentButton, "align right");

        return panel;
    }

    private void setupListeners() {
        addCommentButton.addActionListener(e -> handleAddComment());
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
            List<CommentThread> threads = CommentThread.organizeComments(comments);
            displayThreads(threads);
        }

        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    private void displayThreads(List<CommentThread> threads) {
        for (CommentThread thread : threads) {
            displayThread(thread);
            commentsListPanel.add(Box.createVerticalStrut(themeManager.scale(10)));
        }
    }

    private void displayThread(CommentThread thread) {
        CommentCard card = new CommentCard(thread.getComment());
        commentsListPanel.add(card);
        commentsListPanel.add(Box.createVerticalStrut(themeManager.scale(3)));

        for (CommentThread reply : thread.getReplies()) {
            displayThread(reply);
        }
    }

    private void handleReplyToComment(ReviewComment parentComment) {
        this.replyToComment = parentComment;
        lineNumberField.setText(String.valueOf(parentComment.getLineNumber()));
        lineNumberField.setEnabled(false);
        needsResolutionCheckBox.setEnabled(false);
        addCommentButton.setText("Add Reply");
        commentTextArea.requestFocus();
    }

    private void handleAddComment() {
        if (currentFile == null) {
            showError("Please select a file first");
            return;
        }

        String lineText = lineNumberField.getText().trim();
        String commentText = commentTextArea.getText().trim();

        if (lineText.isEmpty() || commentText.isEmpty()) {
            showError("Please enter both line number and comment");
            return;
        }

        try {
            int lineNumber = Integer.parseInt(lineText);

            String commentId = "COMMENT-" + System.currentTimeMillis();
            String parentId = replyToComment != null ? replyToComment.getId() : null;
            boolean needsResolution = replyToComment == null && needsResolutionCheckBox.isSelected();

            ReviewComment newComment = new ReviewComment(
                commentId,
                currentFile.getPath(),
                lineNumber,
                "Current User",
                commentText,
                "just now",
                parentId,
                needsResolution
            );

            reviewContext.addComment(newComment);

            clearCommentForm();
            loadCommentsForFile(currentFile);

        } catch (NumberFormatException e) {
            showError("Please enter a valid line number");
        }
    }

    private void clearCommentForm() {
        lineNumberField.setText("");
        lineNumberField.setEnabled(true);
        commentTextArea.setText("");
        needsResolutionCheckBox.setSelected(false);
        needsResolutionCheckBox.setEnabled(true);
        addCommentButton.setText("Add Comment");
        replyToComment = null;
    }

    private void showError(String message) {
        ThemedConfirmDialog.showMessage(
            SwingUtilities.getWindowAncestor(this),
            "Error",
            message
        );
    }

    public void setLineNumber(int lineNumber) {
        lineNumberField.setText(String.valueOf(lineNumber));
    }

    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        this.currentFile = null;
        showPlaceholder();
    }
}

