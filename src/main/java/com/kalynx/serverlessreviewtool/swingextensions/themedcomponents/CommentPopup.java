package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * CommentPopup - A themed popup window for adding comments to specific lines
 * Uses ThemedPopupDialog and theme components for consistent styling
 */
public class CommentPopup extends ThemedPopupDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private transient final JTextArea commentTextArea;
    private transient ActionListener submitCallback;

    public CommentPopup(Component parent, String initialText) {
        super(parent, "Add Comment");
        ThemeManager themeManager = ThemeManager.getInstance();

        // Set size
        setDialogSize(350, 220);

        // Get content panel from parent class
        ThemedPanel mainPanel = (ThemedPanel) getContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));

        Theme theme = themeManager.getCurrentTheme();

        // Label - using ThemedLabel
        ThemedLabel label = new ThemedLabel("Comment:");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        mainPanel.add(label, BorderLayout.NORTH);

        // Text area - using ThemedTextArea
        commentTextArea = new ThemedTextArea(5, 0);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setText(initialText);

        // Scroll pane for text area
        JScrollPane scrollPane = new ThemedScrollPane(commentTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel using ThemedButtons
        ThemedPanel buttonPanel = new ThemedPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        ThemedButton submitButton = new ThemedButton("Add Comment");
        submitButton.addActionListener(e -> {
            if (submitCallback != null) {
                submitCallback.actionPerformed(null);
            }
        });

        // Keyboard shortcuts
        InputMap inputMap = commentTextArea.getInputMap();
        ActionMap actionMap = commentTextArea.getActionMap();

        // Escape to cancel
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        actionMap.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // Ctrl+Enter to submit
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "submit");
        actionMap.put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (submitCallback != null) {
                    submitCallback.actionPerformed(null);
                }
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Request focus on text area
        commentTextArea.requestFocusInWindow();
        commentTextArea.selectAll();
    }

    public String getComment() {
        return commentTextArea.getText();
    }

    public void setSubmitCallback(ActionListener callback) {
        this.submitCallback = callback;
    }
}

