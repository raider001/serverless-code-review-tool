package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

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

public class ThemedTextField extends JTextField {
    @Serial
    private static final long serialVersionUID = 1L;

    protected transient final ThemeManager themeManager;
    private boolean isValid = true;
    private transient Validator validator = null;
    private transient Consumer<String> onValidValueSaved = null;
    private transient ThemedValidationOverlay validationOverlay = null;

    private transient ComponentModel<String> model;
    private transient BindingLifecycleHelper.TextBinding textBinding;

    public ThemedTextField() {
        this(0);
    }
    
    public ThemedTextField(int columns) {
        super(columns);
        this.themeManager = ThemeManager.getInstance();
    }
    
    public ThemedTextField(String text) {
        super(text);
        this.themeManager = ThemeManager.getInstance();
    }
    
    public ThemedTextField(String text, int columns) {
        super(text, columns);
        this.themeManager = ThemeManager.getInstance();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand - no caching needed
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());
        setCaretColor(theme.getAccentColor());

        // Add red border if invalid, otherwise empty padding border
        if (!isValid) {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, themeManager.scale(2)),
                BorderFactory.createEmptyBorder(
                    themeManager.scale(3),
                    themeManager.scale(6),
                    themeManager.scale(3),
                    themeManager.scale(6)
                )
            ));
        } else {
            // Simple padding border - no line border for cleaner look
            setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(5),
                themeManager.scale(8),
                themeManager.scale(5),
                themeManager.scale(8)
            ));
        }

        super.paintComponent(g);
    }

    /**
     * Sets up automatic validation for this text field
     *
     * @param validator The validator to use for validation
     * @param onValidValueSaved Callback when a valid value is saved (focus lost)
     * @return this TextField for method chaining
     */
    public ThemedTextField setupValidation(Validator validator, Consumer<String> onValidValueSaved) {
        this.validator = validator;
        this.onValidValueSaved = onValidValueSaved;
        this.validationOverlay = new ThemedValidationOverlay(this);

        // Clear validation error when user starts typing
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

        // Validate and save on focus lost
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateAndSave();
            }
        });

        return this;
    }


    /**
     * Validates the current value and saves if valid
     */
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

    /**
     * Sets the validation state of this text field
     *
     * @param isValid true if the content is valid, false otherwise
     * @param errorMessage optional error message to show in overlay (can be null)
     * @return this TextField for method chaining
     */
    public ThemedTextField setValidationState(boolean isValid, String errorMessage) {
        this.isValid = isValid;

        // Update validation overlay if available
        if (validationOverlay != null) {
            if (isValid) {
                validationOverlay.hideError();
            } else if (errorMessage != null) {
                validationOverlay.showError(errorMessage);
            }
        }

        // Trigger repaint to update visual state (border)
        repaint();

        return this;
    }

    /**
     * Clears any validation error state
     *
     * @return this TextField for method chaining
     */
    public ThemedTextField clearValidationState() {
        return setValidationState(true, null);
    }

    /**
     * Checks if the current content is marked as valid
     *
     * @return true if valid, false if there's a validation error
     */
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
}