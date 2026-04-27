package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;

/**
 * ThemedSpinner - A theme-aware spinner component with full theming support
 * Uses a custom UI similar to ThemedComboBox for seamless theme integration
 */
public class ThemedSpinner extends JSpinner {
    private final ThemeManager themeManager;

    public ThemedSpinner() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeSpinner();
    }

    public ThemedSpinner(SpinnerModel model) {
        super(model);
        this.themeManager = ThemeManager.getInstance();
        initializeSpinner();
    }

    private void initializeSpinner() {
        // Compact sizing to match modern UI
        setPreferredSize(new Dimension(90, 28));
        setMinimumSize(new Dimension(70, 28));
        setMaximumSize(new Dimension(140, 28));

        // Install custom UI similar to ThemedComboBox
        setUI(new ThemedSpinnerUI());

        applyTheme();
    }

    private void applyTheme() {
        if (themeManager == null) {
            return;
        }

        Theme theme = themeManager.getCurrentTheme();

        // Set spinner colors
        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());
        setOpaque(true);

        // Remove outer border for clean look
        setBorder(BorderFactory.createEmptyBorder());

        // Theme the editor (text field inside spinner)
        applyEditorTheme(theme);
    }

    private void applyEditorTheme(Theme theme) {
        JComponent editor = getEditor();
        if (editor instanceof DefaultEditor) {
            JTextField textField = ((DefaultEditor) editor).getTextField();
            textField.setBackground(theme.getInputBackground());
            textField.setForeground(theme.getForegroundColor());
            textField.setCaretColor(theme.getAccentColor());
            textField.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(2),
                themeManager.scale(4),
                themeManager.scale(2),
                themeManager.scale(4)
            ));
            textField.setOpaque(true);
            textField.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(11)));
            textField.setHorizontalAlignment(JTextField.CENTER);
        }
    }

    @Override
    public void updateUI() {
        // Override to prevent Look & Feel from changing our custom UI
        if (themeManager != null) {
            setUI(new ThemedSpinnerUI());
            SwingUtilities.invokeLater(this::applyTheme);
        } else {
            super.updateUI();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getInputBackground());
            setForeground(theme.getForegroundColor());

            // Update editor text field theme as well
            JComponent editor = getEditor();
            if (editor instanceof DefaultEditor) {
                JTextField textField = ((DefaultEditor) editor).getTextField();
                textField.setBackground(theme.getInputBackground());
                textField.setForeground(theme.getForegroundColor());
                textField.setCaretColor(theme.getAccentColor());
            }
        }
        super.paintComponent(g);
    }

    /**
     * Custom UI for the spinner that respects theme colors, similar to ThemedComboBox
     */
    private class ThemedSpinnerUI extends BasicSpinnerUI {

        @Override
        protected Component createNextButton() {
            JButton button = new JButton("▲") {
                @Override
                public void paintComponent(Graphics g) {
                    Theme theme = themeManager.getCurrentTheme();

                    // Paint button background matching input
                    g.setColor(theme.getInputBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Paint arrow symbol
                    g.setColor(theme.getForegroundColor());
                    g.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(8)));
                    FontMetrics fm = g.getFontMetrics();
                    String text = getText();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g.drawString(text, x, y);
                }
            };

            button.setName("Spinner.nextButton");
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setPreferredSize(new Dimension(themeManager.scale(18), themeManager.scale(14)));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            installNextButtonListeners(button);
            return button;
        }

        @Override
        protected Component createPreviousButton() {
            JButton button = new JButton("▼") {
                @Override
                public void paintComponent(Graphics g) {
                    Theme theme = themeManager.getCurrentTheme();

                    // Paint button background matching input
                    g.setColor(theme.getInputBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Paint arrow symbol
                    g.setColor(theme.getForegroundColor());
                    g.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(8)));
                    FontMetrics fm = g.getFontMetrics();
                    String text = getText();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g.drawString(text, x, y);
                }
            };

            button.setName("Spinner.previousButton");
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setPreferredSize(new Dimension(themeManager.scale(18), themeManager.scale(14)));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            installPreviousButtonListeners(button);
            return button;
        }
    }
}




