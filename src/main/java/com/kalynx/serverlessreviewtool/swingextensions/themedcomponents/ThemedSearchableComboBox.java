package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.components.ComboListener;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

public class ThemedSearchableComboBox extends JComboBox<String> {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager;
    private final Vector<String> myVector = new Vector<>();
    private transient final ComboListener<String> comboListener;

    private transient ComponentModel<List<String>> optionsModel;
    private transient Consumer<List<String>> optionsChangeListener;

    public ThemedSearchableComboBox(List<String> items) {
        super();
        this.themeManager = ThemeManager.getInstance();

        setModel(new DefaultComboBoxModel<>(myVector));
        setSelectedIndex(-1);
        setEditable(true);

        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        setRenderer(new ThemedComboBoxRenderer());
        setOpaque(true);
        setFocusable(true);

        setUI(new ThemedComboBoxUI());

        JTextField text = (JTextField) this.getEditor().getEditorComponent();
        text.setFocusable(true);
        text.setText("");

        comboListener = new ComboListener<>(this, myVector);
        text.addKeyListener(comboListener);

        setValues(items);
        applyTheme();
        applyEditorTheme(text);
    }

    public void setOnApply(Consumer<String> onApplyCallback) {
        comboListener.setOnApply(onApplyCallback);
    }

    public void setValues(List<String> values) {
        myVector.clear();
        myVector.addAll(values);
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());
        setOpaque(true);

        setBorder(BorderFactory.createEmptyBorder(
            themeManager.scale(2),
            themeManager.scale(4),
            themeManager.scale(2),
            themeManager.scale(4)
        ));
    }

    private void applyEditorTheme(JTextField editor) {
        Theme theme = themeManager.getCurrentTheme();
        editor.setBackground(theme.getInputBackground());
        editor.setForeground(theme.getForegroundColor());
        editor.setCaretColor(theme.getForegroundColor());
        editor.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(
                themeManager.scale(2),
                themeManager.scale(4),
                themeManager.scale(2),
                themeManager.scale(4)
            )
        ));
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (themeManager != null) {
            SwingUtilities.invokeLater(() -> {
                setUI(new ThemedComboBoxUI());
                applyTheme();
                JTextField editor = (JTextField) getEditor().getEditorComponent();
                if (editor != null) {
                    applyEditorTheme(editor);
                    if (!java.util.Arrays.asList(editor.getKeyListeners()).contains(comboListener)) {
                        editor.addKeyListener(comboListener);
                    }
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getInputBackground());
            setForeground(theme.getForegroundColor());
        }
        super.paintComponent(g);
    }

    private class ThemedComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("▼") {
                @Override
                public void paintComponent(Graphics g) {
                    Theme theme = themeManager.getCurrentTheme();

                    g.setColor(theme.getInputBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());

                    g.setColor(theme.getForegroundColor());
                    g.setFont(getFont());
                    FontMetrics fm = g.getFontMetrics();
                    String text = getText();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g.drawString(text, x, y);
                }
            };

            button.setName("ComboBox.arrowButton");
            button.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(10)));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Theme theme = themeManager.getCurrentTheme();
            g.setColor(theme.getInputBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private class ThemedComboBoxRenderer extends DefaultListCellRenderer {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Theme theme = themeManager.getCurrentTheme();

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

    public void bindTo(ComponentModel<List<String>> optionsModel) {
        unbind();
        this.optionsModel = optionsModel;

        if (optionsModel.getValue() != null) {
            setValues(optionsModel.getValue());
        }

        optionsChangeListener = options -> {
            if (options != null) {
                setValues(options);
            }
        };
        optionsModel.addChangeListener(optionsChangeListener);

        BindingLifecycleHelper.setupAutoUnbind(this, this::unbind);
    }

    public void unbind() {
        if (optionsModel != null && optionsChangeListener != null) {
            optionsModel.removeChangeListener(optionsChangeListener);
        }
        optionsModel = null;
        optionsChangeListener = null;
    }
}




