package com.kalynx.serverlessreviewtool.swingextensions;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BindingLifecycleHelper {

    public static void setupAutoUnbind(JComponent component, Runnable unbindAction) {
        component.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) { }
            public void ancestorMoved(AncestorEvent event) { }
            public void ancestorRemoved(AncestorEvent event) {
                unbindAction.run();
            }
        });
    }

    public static class TextBinding {
        public Consumer<String> modelChangeListener;
        public DocumentListener modelSyncListener;
    }

    public static TextBinding setupTextBinding(
            ComponentModel<String> model,
            Supplier<String> getText,
            Consumer<String> setText,
            Document document) {

        TextBinding binding = new TextBinding();

        setText.accept(model.getValue() != null ? model.getValue() : "");

        binding.modelChangeListener = value -> {
            String newValue = value != null ? value : "";
            if (!getText.get().equals(newValue)) {
                setText.accept(newValue);
            }
        };
        model.addChangeListener(binding.modelChangeListener);

        binding.modelSyncListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { model.setValue(getText.get()); }
            public void removeUpdate(DocumentEvent e) { model.setValue(getText.get()); }
            public void changedUpdate(DocumentEvent e) { model.setValue(getText.get()); }
        };
        document.addDocumentListener(binding.modelSyncListener);

        return binding;
    }

    public static void unbindText(
            ComponentModel<String> model,
            Consumer<String> modelChangeListener,
            Document document,
            DocumentListener modelSyncListener) {

        if (model != null && modelChangeListener != null) {
            model.removeChangeListener(modelChangeListener);
        }
        if (modelSyncListener != null) {
            document.removeDocumentListener(modelSyncListener);
        }
    }
}

