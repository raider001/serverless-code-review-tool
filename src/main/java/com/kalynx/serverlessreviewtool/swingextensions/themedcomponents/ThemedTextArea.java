package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.utils.Validator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class ThemedTextArea extends JTextArea {

    private final ThemeManager themeManager;
    private boolean isValid = true;
    private transient Validator validator = null;
    private transient Consumer<String> onValidValueSaved = null;
    private transient ThemedValidationOverlay validationOverlay = null;
    private transient String valueOnFocusGained = null;

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

    public void setupValidation(Validator validator, Consumer<String> onValidValueSaved) {
        this.validator = validator;
        this.onValidValueSaved = onValidValueSaved;
        this.validationOverlay = new ThemedValidationOverlay(this);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearValidationState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearValidationState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearValidationState();
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                valueOnFocusGained = getText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentValue = getText();
                if (!currentValue.equals(valueOnFocusGained)) {
                    validateAndSave();
                }
            }
        });

    }

    private void validateAndSave() {
        if (validator == null || onValidValueSaved == null) {
            return;
        }

        String value = getText().trim();
        Validator.ValidationResult result = validator.validate(value);

        if (result.isValid()) {
            clearValidationState();
            onValidValueSaved.accept(value);
        } else {
            setValidationState(false, result.getErrorMessage());
        }
    }

    public ThemedTextArea setValidationState(boolean isValid, String errorMessage) {
        this.isValid = isValid;

        if (validationOverlay != null) {
            if (isValid) {
                validationOverlay.hideError();
            } else if (errorMessage != null) {
                validationOverlay.showError(errorMessage);
            }
        }

        repaint();
        return this;
    }

    public ThemedTextArea clearValidationState() {
        return setValidationState(true, null);
    }

    public boolean isValid() {
        return isValid;
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



