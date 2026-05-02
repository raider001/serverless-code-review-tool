package com.kalynx.serverlessreviewtool.swingextensions.components;

import java.io.Serial;

import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

public class SearchableCombobox<T> extends JComboBox<T> {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Vector<T> myVector = new Vector<>();
    private transient final ComboListener<T> comboListener;

    public SearchableCombobox() {
        setModel(new DefaultComboBoxModel<>(myVector));
        setSelectedIndex(-1);
        setEditable(true);
        JTextField text = (JTextField) this.getEditor().getEditorComponent();
        text.setFocusable(true);
        text.setText("");
        comboListener = new ComboListener<>(this, myVector);
        text.addKeyListener(comboListener);
    }

    public void setOnApply(Consumer<T> onApplyCallback) {
        comboListener.setOnApply(onApplyCallback);
    }

    public void setValues(List<T> values) {
        myVector.clear();
        myVector.addAll(values);
    }
}