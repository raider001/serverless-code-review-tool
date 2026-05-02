package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

public class ThemedTextArea extends JTextArea {

    private final ThemeManager themeManager;

    private ComponentModel<String> model;
    private BindingLifecycleHelper.TextBinding textBinding;

    public ThemedTextArea() {
        this(0, 0);
    }

    public ThemedTextArea(int rows, int columns) {
        super(rows, columns);
        this.themeManager = ThemeManager.getInstance();
        setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(12)));
        setLineWrap(false);
        setWrapStyleWord(false);
        setMargin(new Insets(themeManager.scale(5), themeManager.scale(5), themeManager.scale(5), themeManager.scale(5)));
    }

    public ThemedTextArea(String text) {
        this();
        setText(text);
    }

    public ThemedTextArea(String text, int rows, int columns) {
        this(rows, columns);
        setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (themeManager != null) {
            Theme theme = themeManager.getCurrentTheme();
            setBackground(theme.getInputBackground());
            setForeground(theme.getForegroundColor());
            setCaretColor(theme.getAccentColor());
            setSelectionColor(theme.getAccentColor());
            setSelectedTextColor(Color.WHITE);
        }
        super.paintComponent(g);
    }

    public void bindTo(ComponentModel<String> model) {
        unbind();
        this.model = model;

        textBinding = BindingLifecycleHelper.setupTextBinding(
            model,
            this::getText,
            this::setText,
            getDocument()
        );

        BindingLifecycleHelper.setupAutoUnbind(this, this::unbind);
    }

    public void unbind() {
        if (textBinding != null) {
            BindingLifecycleHelper.unbindText(
                model,
                textBinding.modelChangeListener,
                getDocument(),
                textBinding.modelSyncListener
            );
        }
        model = null;
        textBinding = null;
    }

    public boolean isBound() {
        return model != null;
    }
}



