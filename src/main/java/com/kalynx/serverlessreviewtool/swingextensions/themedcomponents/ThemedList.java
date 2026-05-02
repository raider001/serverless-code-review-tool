package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * A JList that automatically applies and updates theme colors
 * Colors are queried on-demand during paint for automatic theme updates
 */
public class ThemedList<T> extends JList<T> {
    private static final long serialVersionUID = 1L;
    
    protected transient final ThemeManager themeManager;
    
    public ThemedList() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    public ThemedList(T[] items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    public ThemedList(Vector<? extends T> items) {
        super(items);
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    public ThemedList(ListModel<T> model) {
        super(model);
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    private void initializeDefaults() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Don't set fixed cell height - let it size to content
        setFixedCellHeight(-1);
        // Calculate optimal row height based on font metrics
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
    }

    /**
     * Set a callback to be invoked when an item is selected
     * Handles the valueIsAdjusting check internally
     *
     * @param callback The callback to invoke with the selected item
     */
    public void onItemSelected(Consumer<T> callback) {
        addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                T selected = getSelectedValue();
                if (selected != null && callback != null) {
                    callback.accept(selected);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));

        super.paintComponent(g);
    }
}

