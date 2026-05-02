package com.kalynx.serverlessreviewtool.swingextensions.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ComboListener<T> extends KeyAdapter
{

    JComboBox<T> cbListener;
    Vector<T> vector;
    Consumer<T> onApply;

    public ComboListener(JComboBox<T> cbListenerParam, Vector<T> vectorParam)
    {
        cbListener = cbListenerParam;
        vector = vectorParam;
        onApply = null;
    }

    public ComboListener(JComboBox<T> cbListenerParam, Vector<T> vectorParam, Consumer<T> onApplyCallback)
    {
        cbListener = cbListenerParam;
        vector = vectorParam;
        onApply = onApplyCallback;
    }

    public void setOnApply(Consumer<T> onApplyCallback)
    {
        onApply = onApplyCallback;
    }

    @Override
    public void keyPressed(KeyEvent key)
    {
        int keyCode = key.getKeyCode();

        if (keyCode == KeyEvent.VK_ENTER) {
            if (cbListener.getSelectedItem() != null && cbListener.isPopupVisible()) {
                @SuppressWarnings("unchecked")
                T selectedItem = (T) cbListener.getSelectedItem();
                if (onApply != null) {
                    onApply.accept(selectedItem);
                }
                cbListener.hidePopup();
            }
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            cbListener.hidePopup();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void keyReleased(KeyEvent key)
    {
        int keyCode = key.getKeyCode();

        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE ||
            keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
            keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
            return;
        }

        JTextField textField = (JTextField)key.getSource();
        String text = textField.getText();

        cbListener.setModel(new DefaultComboBoxModel(getFilteredList(text)));
        cbListener.setSelectedIndex(-1);
        ((JTextField)cbListener.getEditor().getEditorComponent()).setText(text);
        cbListener.showPopup();
    }

    public Vector<T> getFilteredList(String text)
    {
        Vector<T> v = new Vector<>();
        for (T o : vector) {
            if (o.toString().toLowerCase().startsWith(text.toLowerCase())) {
                v.add(o);
            }
        }
        return v;
    }
}