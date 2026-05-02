package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * ThemedCheckBox - A theme-aware checkbox component
 */
public class ThemedCheckBox extends JCheckBox {
    private final ThemeManager themeManager;

    private ComponentModel<Boolean> model;
    private BindingLifecycleHelper.CheckBoxBinding binding;

    public ThemedCheckBox(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
        applyTheme();
    }

    public ThemedCheckBox(String text, boolean selected) {
        super(text, selected);
        this.themeManager = ThemeManager.getInstance();
        applyTheme();
    }

    public ThemedCheckBox() {
        super();
        this.themeManager = ThemeManager.getInstance();
        applyTheme();
    }

    private void applyTheme() {
        if (themeManager == null) {
            return;
        }

        Theme theme = themeManager.getCurrentTheme();
        setForeground(theme.getForegroundColor());
        setBackground(theme.getBackgroundColor());
        setOpaque(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setForeground(theme.getForegroundColor());
            setBackground(theme.getBackgroundColor());
        }
        super.paintComponent(g);
    }

    public void bindTo(ComponentModel<Boolean> model) {
        unbind();
        this.model = model;

        binding = BindingLifecycleHelper.setupCheckBoxBinding(model, this);

        BindingLifecycleHelper.setupAutoUnbind(this, this::unbind);
    }

    public void unbind() {
        if (binding != null) {
            BindingLifecycleHelper.unbindCheckBox(
                model,
                binding.modelChangeListener,
                this,
                binding.itemListener
            );
        }
        model = null;
        binding = null;
    }
}



