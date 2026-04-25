package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * CommentPopup - A themed popup window for adding comments to specific lines
 * Uses ThemedPopupDialog for consistent styling with custom titlebar
 */
public class CommentPopup extends ThemedPopupDialog {
    private final JTextArea commentTextArea;
    private ActionListener submitCallback;

    public CommentPopup(Component parent, String initialText) {
        super(parent, "Add Comment");
        ThemeManager themeManager = ThemeManager.getInstance();

        // Set size
        setDialogSize(350, 220);

        // Get content panel from parent class
        JPanel mainPanel = getContentPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));

        Theme theme = themeManager.getCurrentTheme();

        // Label
        JLabel label = new JLabel("Comment:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12));
        mainPanel.add(label, BorderLayout.NORTH);

        // Text area
        commentTextArea = new JTextArea(5, 0);
        commentTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setText(initialText);
        commentTextArea.setBackground(theme.getInputBackground());
        commentTextArea.setForeground(theme.getForegroundColor());

        // Scroll pane for text area
        JScrollPane scrollPane = new ThemedScrollPane(commentTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel using modern styled buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = createModernButton("Cancel", theme, false);
        cancelButton.addActionListener(e -> dispose());

        JButton submitButton = createModernButton("Add Comment", theme, true);
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

    /**
     * Create a modern flat-design button with smooth styling
     */
    private JButton createModernButton(String text, Theme theme, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isPrimary) {
            // Primary button - solid filled with accent color
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBackground(theme.getAccentColor());
        } else {
            // Secondary button - text only, subtle styling
            button.setForeground(theme.getForegroundColor());
        }

        // Custom rendering for modern look
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    // Brighten accent color on hover
                    button.setBackground(new Color(
                            Math.min(255, theme.getAccentColor().getRed() + 25),
                            Math.min(255, theme.getAccentColor().getGreen() + 25),
                            Math.min(255, theme.getAccentColor().getBlue() + 25)
                    ));
                } else {
                    // Underline effect for secondary button
                    button.setBorderPainted(true);
                    button.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, theme.getAccentColor()));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(theme.getAccentColor());
                } else {
                    button.setBorderPainted(false);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    // Darker on press
                    button.setBackground(new Color(
                            Math.max(0, theme.getAccentColor().getRed() - 15),
                            Math.max(0, theme.getAccentColor().getGreen() - 15),
                            Math.max(0, theme.getAccentColor().getBlue() - 15)
                    ));
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(theme.getAccentColor());
                }
            }
        });

        return button;
    }
}


