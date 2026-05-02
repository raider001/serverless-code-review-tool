package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedRadioButton - A JRadioButton that automatically applies and updates theme colors
 */
public class ThemedRadioButton extends JRadioButton {

    protected final ThemeManager themeManager;

    private ComponentModel<?> model;
    private BindingLifecycleHelper.RadioButtonBinding<?> binding;
    private Object valueWhenSelected;

    public ThemedRadioButton() {
        super();
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    public ThemedRadioButton(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    public ThemedRadioButton(String text, boolean selected) {
        super(text, selected);
        this.themeManager = ThemeManager.getInstance();
        initializeDefaults();
    }

    private void initializeDefaults() {
        applyTheme();
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());

        super.paintComponent(g);
    }

    public <T> void bindTo(ComponentModel<T> model, T valueWhenSelected) {
        unbind();
        this.model = model;
        this.valueWhenSelected = valueWhenSelected;

        this.binding = BindingLifecycleHelper.setupRadioButtonBinding(model, this, valueWhenSelected);

        BindingLifecycleHelper.setupAutoUnbind(this, this::unbind);
    }

    @SuppressWarnings("unchecked")
    public void unbind() {
        if (binding != null && model != null) {
            BindingLifecycleHelper.unbindRadioButton(
                (ComponentModel<Object>) model,
                (java.util.function.Consumer<Object>) binding.modelChangeListener,
                this,
                binding.actionListener
            );
        }
        model = null;
        binding = null;
        valueWhenSelected = null;
    }
}

