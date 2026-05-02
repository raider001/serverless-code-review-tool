package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.util.Vector;

/**
 * ThemedComboBox - A simple JComboBox that queries theme colors on-demand
 * Provides clean, automatic theme integration
 */
public class ThemedComboBox<T> extends JComboBox<T> {

    private final ThemeManager themeManager;

    public ThemedComboBox() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    public ThemedComboBox(T[] items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    public ThemedComboBox(Vector<T> items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    public ThemedComboBox(ComboBoxModel<T> model) {
        super(model);
        this.themeManager = ThemeManager.getInstance();
        initialize();
    }

    /**
     * Initialize basic styling
     */
    private void initialize() {
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        setRenderer(new ThemedComboBoxRenderer());
        setOpaque(true);
        setFocusable(true);

        // Install custom UI that respects our theme colors
        setUI(new ThemedComboBoxUI());

        applyTheme();
    }

    /**
     * Apply theme colors to the combobox
     */
    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();

        Color inputBg = theme.getInputBackground();
        Color fg = theme.getForegroundColor();

        // Set colors
        setBackground(inputBg);
        setForeground(fg);
        setOpaque(true);

        // No border for cleaner look
        setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(2),
            themeManager.scale(4),
            themeManager.scale(2),
            themeManager.scale(4)
        ));
    }

    @Override
    public void updateUI() {
        // Override to prevent Look & Feel from changing our custom UI
        if (themeManager != null) {
            setUI(new ThemedComboBoxUI());
            SwingUtilities.invokeLater(this::applyTheme);
        } else {
            super.updateUI();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on every paint for theme switching
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getInputBackground());
            setForeground(theme.getForegroundColor());
        }
        super.paintComponent(g);
    }

    /**
     * Custom UI for the combobox that respects theme colors
     */
    private class ThemedComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            // Create button with down arrow symbol that queries theme on every paint
            JButton button = new JButton("▼") {
                @Override
                public void paintComponent(Graphics g) {
                    // Query theme colors on every paint for theme switching
                    Theme theme = themeManager.getCurrentTheme();

                    // Paint button background
                    g.setColor(theme.getInputBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Paint arrow text
                    g.setColor(theme.getForegroundColor());
                    g.setFont(getFont());
                    FontMetrics fm = g.getFontMetrics();
                    String text = getText();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g.drawString(text, x, y);
                }
            };

            button.setName("ComboBox.arrowButton"); // Important for UI delegate
            button.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setContentAreaFilled(false); // We paint manually
            button.setOpaque(false); // We paint manually
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Paint themed background for the display area - query on every paint
            Theme theme = themeManager.getCurrentTheme();
            g.setColor(theme.getInputBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    /**
     * Simple themed renderer for dropdown items
     */
    private class ThemedComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Theme theme = themeManager.getCurrentTheme();

            // Theme the list itself
            if (list != null) {
                list.setBackground(theme.getInputBackground());
                list.setForeground(theme.getForegroundColor());
                list.setSelectionBackground(theme.getAccentColor());
                list.setSelectionForeground(Color.WHITE);
            }

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
            label.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(4),
                themeManager.scale(8),
                themeManager.scale(4),
                themeManager.scale(8)
            ));
            label.setOpaque(true);

            // Apply theme colors - MUST set after calling super
            if (isSelected) {
                label.setBackground(theme.getAccentColor());
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(theme.getInputBackground());
                label.setForeground(theme.getForegroundColor());
            }

            return label;
        }
    }
}
