package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.WindowResizeHandler;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.theme.icons.AlertIcon;
import com.kalynx.serverlessreviewtool.theme.icons.CheckIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class InlineCommentDialog extends JDialog {

    private final ThemeManager themeManager = ThemeManager.getInstance();
    private final ReviewContext reviewContext;
    private final ReviewFile file;
    private final int lineNumber;
    private final Runnable onCommentAdded;

    private ThemedPanel commentsContainer;
    private ThemedTextArea newCommentArea;
    private ThemedButton resolveToggleButton;
    private ThemedButton codeButton;
    private ThemedPanel headerPanel;
    private boolean conversationNeedsResolution = false;
    private boolean conversationResolved = false;

    public InlineCommentDialog(Window owner, ReviewContext reviewContext, ReviewFile file,
                               int lineNumber, Runnable onCommentAdded) {
        super(owner, ModalityType.MODELESS);
        this.reviewContext = reviewContext;
        this.file = file;
        this.lineNumber = lineNumber;
        this.onCommentAdded = onCommentAdded;

        setUndecorated(true);
        initComponents();
        setupKeyboardShortcuts();
        loadExistingComments();
        applyTheme();

        WindowResizeHandler resizeHandler = new WindowResizeHandler(this, 5);
        addMouseListener(resizeHandler);
        addMouseMotionListener(resizeHandler);

        setMinimumSize(new Dimension(500, 350));
        pack();
        setSize(600, 450);
        setLocationRelativeTo(owner);
        setResizable(true);
    }

    private void setupKeyboardShortcuts() {
        JRootPane rootPane = getRootPane();

        rootPane.registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        newCommentArea.registerKeyboardAction(
            e -> handleAddComment(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_FOCUSED
        );
    }

    private void initComponents() {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("fill, insets 0", "[grow]", "[][][grow]"));

        Theme theme = themeManager.getCurrentTheme();
        contentPanel.setBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 2));

        String fileName = file.getPath().substring(file.getPath().lastIndexOf('/') + 1);
        CustomTitleBar titleBar = new CustomTitleBar(this, fileName + " : Line " + lineNumber);
        contentPanel.add(titleBar, "growx, wrap");

        headerPanel = new ThemedPanel(new MigLayout("fill, insets 4", "[grow]", "[]"));
        headerPanel.setVisible(false);
        contentPanel.add(headerPanel, "growx, wrap");

        commentsContainer = new ThemedPanel();
        commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));

        ThemedScrollPane scrollPane = new ThemedScrollPane(commentsContainer);
        scrollPane.setMinimumSize(new Dimension(200, 100));

        ThemedPanel inputPanel = createInputPanel();

        ThemedSplitPane splitPane = new ThemedSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, inputPanel);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(6);
        splitPane.setDividerLocation(300);

        contentPanel.add(splitPane, "grow");

        setContentPane(contentPanel);
    }

    private ThemedPanel createInputPanel() {
        ThemedPanel panel = new ThemedPanel(new MigLayout("fill, insets 6", "[120!][10!][40!][10!][grow][100!]", "[grow]3[]"));

        newCommentArea = new ThemedTextArea(3, 40);
        newCommentArea.setLineWrap(true);
        newCommentArea.setWrapStyleWord(true);
        newCommentArea.setToolTipText("Press Ctrl+Enter to submit. Use ```code``` for code snippets. Drag divider above to resize.");
        newCommentArea.setMinimumSize(new Dimension(200, themeManager.scale(70)));
        ThemedScrollPane textScrollPane = new ThemedScrollPane(newCommentArea);
        panel.add(textScrollPane, "grow, span 6, wrap, wmin 200");

        resolveToggleButton = new ThemedButton("Mark as Needs Resolution");
        resolveToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
        resolveToggleButton.addActionListener(e -> handleResolveToggle());
        panel.add(resolveToggleButton);

        panel.add(new ThemedLabel(""));

        codeButton = new ThemedButton("<>");
        codeButton.setFont(new Font("Consolas", Font.BOLD, themeManager.scale(11)));
        codeButton.setToolTipText("Insert code snippet");
        codeButton.addActionListener(e -> handleInsertCode());
        panel.add(codeButton);

        panel.add(new ThemedLabel(""));

        panel.add(new ThemedLabel(""), "grow");

        ThemedButton addButton = new ThemedButton("Add Comment");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(10)));
        addButton.addActionListener(e -> handleAddComment());
        panel.add(addButton);

        return panel;
    }

    private void handleInsertCode() {
        String currentText = newCommentArea.getText();
        int caretPos = newCommentArea.getCaretPosition();

        String codeBlock = "```\n\n```";
        String newText = currentText.substring(0, caretPos) + codeBlock + currentText.substring(caretPos);

        newCommentArea.setText(newText);
        newCommentArea.setCaretPosition(caretPos + 4);
        newCommentArea.requestFocus();
    }

    private void loadExistingComments() {
        commentsContainer.removeAll();

        List<ReviewComment> allComments = reviewContext.getCommentsForFile(file.getPath());
        List<ReviewComment> lineComments = allComments.stream()
            .filter(c -> c.getLineNumber() == lineNumber)
            .collect(Collectors.toList());

        conversationNeedsResolution = !lineComments.isEmpty() &&
            lineComments.stream().anyMatch(c -> c.needsResolution());
        conversationResolved = conversationNeedsResolution &&
            lineComments.stream().filter(c -> c.needsResolution()).allMatch(c -> c.isResolved());

        updateResolutionUI();

        if (lineComments.isEmpty()) {
            ThemedPanel placeholderPanel = new ThemedPanel(new MigLayout("fill, insets 10", "[center]", "[center]"));
            ThemedLabel noComments = new ThemedLabel("<html><div style='text-align: center;'>" +
                    "<b>No comments yet</b><br>" +
                    "Start the conversation below." +
                    "</div></html>");
            noComments.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(11)));
            placeholderPanel.add(noComments, "cell 0 0");
            commentsContainer.add(placeholderPanel);
        } else {
            for (ReviewComment comment : lineComments) {
                commentsContainer.add(new CommentCard(comment));
                commentsContainer.add(Box.createVerticalStrut(themeManager.scale(3)));
            }
        }

        commentsContainer.add(Box.createVerticalGlue());
        commentsContainer.revalidate();
        commentsContainer.repaint();
    }

    private void updateResolutionUI() {
        Theme theme = themeManager.getCurrentTheme();

        headerPanel.removeAll();
        headerPanel.setLayout(new MigLayout("fill, insets 8", "[]4[]push", "[]"));

        if (conversationNeedsResolution) {
            headerPanel.setVisible(true);

            Color bgColor = conversationResolved
                ? new Color(76, 175, 80, 30)
                : new Color(255, 152, 0, 30);
            Color borderColor = conversationResolved
                ? new Color(76, 175, 80)
                : new Color(255, 152, 0);
            Color textColor = conversationResolved
                ? new Color(76, 175, 80)
                : new Color(255, 152, 0);

            headerPanel.setBackground(bgColor);
            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

            Icon icon = conversationResolved
                ? new CheckIcon(themeManager.scale(16), borderColor)
                : new AlertIcon(themeManager.scale(16), borderColor);
            String status = conversationResolved ? "Resolved" : "Needs Resolution";

            ThemedLabel iconLabel = new ThemedLabel();
            iconLabel.setIcon(icon);
            headerPanel.add(iconLabel, "align left");

            ThemedLabel statusLabel = new ThemedLabel(status);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(12)));
            statusLabel.setForeground(textColor);
            headerPanel.add(statusLabel, "align left");

            if (conversationResolved) {
                ThemedLabel resolvedByLabel = new ThemedLabel("• Marked resolved by Current User");
                resolvedByLabel.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
                resolvedByLabel.setForeground(theme.getSecondaryTextColor());
                headerPanel.add(resolvedByLabel, "gapleft 6");
            }
        } else {
            headerPanel.setVisible(false);
        }

        if (!conversationNeedsResolution) {
            resolveToggleButton.setText("Mark as Needs Resolution");
        } else if (conversationResolved) {
            resolveToggleButton.setText("Mark as Unresolved");
        } else {
            resolveToggleButton.setText("Mark as Resolved");
        }

        headerPanel.revalidate();
        headerPanel.repaint();
    }

    private void handleResolveToggle() {
        List<ReviewComment> lineComments = reviewContext.getCommentsForFile(file.getPath()).stream()
            .filter(c -> c.getLineNumber() == lineNumber)
            .collect(Collectors.toList());

        if (!conversationNeedsResolution) {
            for (ReviewComment comment : lineComments) {
                comment.setNeedsResolution(true);
            }
        } else if (conversationResolved) {
            for (ReviewComment comment : lineComments) {
                comment.markUnresolved();
            }
        } else {
            for (ReviewComment comment : lineComments) {
                comment.markResolved("Current User");
            }
        }

        loadExistingComments();
        if (onCommentAdded != null) {
            onCommentAdded.run();
        }
    }

    private void handleAddComment() {
        String commentText = newCommentArea.getText().trim();

        if (commentText.isEmpty()) {
            ThemedConfirmDialog.showMessage(this, "Error", "Please enter a comment");
            return;
        }

        String commentId = "COMMENT-" + System.currentTimeMillis();

        ReviewComment newComment = new ReviewComment(
            commentId,
            file.getPath(),
            lineNumber,
            "Current User",
            commentText,
            "just now",
            null,
            conversationNeedsResolution
        );

        reviewContext.addComment(newComment);

        newCommentArea.setText("");

        loadExistingComments();

        if (onCommentAdded != null) {
            onCommentAdded.run();
        }

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) commentsContainer.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        getContentPane().setBackground(theme.getBackgroundColor());
    }
}





























