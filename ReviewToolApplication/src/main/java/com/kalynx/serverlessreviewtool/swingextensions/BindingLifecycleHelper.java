package com.kalynx.serverlessreviewtool.swingextensions;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
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
            SwingUtilities.invokeLater(() -> {
                String newValue = value != null ? value : "";
                if (!getText.get().equals(newValue)) {
                    setText.accept(newValue);
                }
            });
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

    public static class ComboBoxBinding<T> {
        public Consumer<T> valueChangeListener;
        public Consumer<List<T>> optionsChangeListener;
        public ActionListener selectionListener;
    }

    public static <T> ComboBoxBinding<T> setupComboBoxBinding(
            ComponentModel<T> valueModel,
            ComponentModel<List<T>> optionsModel,
            JComboBox<T> comboBox) {

        ComboBoxBinding<T> binding = new ComboBoxBinding<>();

        if (optionsModel != null) {
            updateComboBoxOptions(comboBox, optionsModel.getValue());

            binding.optionsChangeListener = options -> {
                SwingUtilities.invokeLater(() ->
                    updateComboBoxOptions(comboBox, options));
            };
            optionsModel.addChangeListener(binding.optionsChangeListener);
        }

        if (valueModel != null) {
            comboBox.setSelectedItem(valueModel.getValue());

            binding.valueChangeListener = value -> {
                SwingUtilities.invokeLater(() -> {
                    if (!areEqual(comboBox.getSelectedItem(), value)) {
                        comboBox.setSelectedItem(value);
                    }
                });
            };
            valueModel.addChangeListener(binding.valueChangeListener);

            binding.selectionListener = ignored -> {
                @SuppressWarnings("unchecked")
                T selected = (T) comboBox.getSelectedItem();
                valueModel.setValue(selected);
            };
            comboBox.addActionListener(binding.selectionListener);
        }

        return binding;
    }

    public static <T> void unbindComboBox(
            ComponentModel<T> valueModel,
            ComponentModel<List<T>> optionsModel,
            Consumer<T> valueChangeListener,
            Consumer<List<T>> optionsChangeListener,
            JComboBox<T> comboBox,
            ActionListener selectionListener) {

        if (valueModel != null && valueChangeListener != null) {
            valueModel.removeChangeListener(valueChangeListener);
        }
        if (optionsModel != null && optionsChangeListener != null) {
            optionsModel.removeChangeListener(optionsChangeListener);
        }
        if (selectionListener != null) {
            comboBox.removeActionListener(selectionListener);
        }
    }

    public static class CheckBoxBinding {
        public Consumer<Boolean> modelChangeListener;
        public ItemListener itemListener;
    }

    public static CheckBoxBinding setupCheckBoxBinding(
            ComponentModel<Boolean> model,
            JCheckBox checkBox) {

        CheckBoxBinding binding = new CheckBoxBinding();

        checkBox.setSelected(model.getValue() != null ? model.getValue() : false);

        binding.modelChangeListener = value -> {
            SwingUtilities.invokeLater(() -> {
                boolean newValue = value != null ? value : false;
                if (checkBox.isSelected() != newValue) {
                    checkBox.setSelected(newValue);
                }
            });
        };
        model.addChangeListener(binding.modelChangeListener);

        binding.itemListener = ignored ->
            model.setValue(checkBox.isSelected());
        checkBox.addItemListener(binding.itemListener);

        return binding;
    }

    public static void unbindCheckBox(
            ComponentModel<Boolean> model,
            Consumer<Boolean> modelChangeListener,
            JCheckBox checkBox,
            ItemListener itemListener) {

        if (model != null && modelChangeListener != null) {
            model.removeChangeListener(modelChangeListener);
        }
        if (itemListener != null) {
            checkBox.removeItemListener(itemListener);
        }
    }

    public static class RadioButtonBinding<T> {
        public Consumer<T> modelChangeListener;
        public ActionListener actionListener;
    }

    public static <T> RadioButtonBinding<T> setupRadioButtonBinding(
            ComponentModel<T> model,
            JRadioButton radioButton,
            T valueWhenSelected) {

        RadioButtonBinding<T> binding = new RadioButtonBinding<>();

        radioButton.setSelected(areEqual(model.getValue(), valueWhenSelected));

        binding.modelChangeListener = value -> {
            SwingUtilities.invokeLater(() -> {
                boolean shouldBeSelected = areEqual(value, valueWhenSelected);
                if (radioButton.isSelected() != shouldBeSelected) {
                    radioButton.setSelected(shouldBeSelected);
                }
            });
        };
        model.addChangeListener(binding.modelChangeListener);

        binding.actionListener = ignored -> {
            if (radioButton.isSelected()) {
                model.setValue(valueWhenSelected);
            }
        };
        radioButton.addActionListener(binding.actionListener);

        return binding;
    }

    public static <T> void unbindRadioButton(
            ComponentModel<T> model,
            Consumer<T> modelChangeListener,
            JRadioButton radioButton,
            ActionListener actionListener) {

        if (model != null && modelChangeListener != null) {
            model.removeChangeListener(modelChangeListener);
        }
        if (actionListener != null) {
            radioButton.removeActionListener(actionListener);
        }
    }

    private static <T> void updateComboBoxOptions(JComboBox<T> comboBox, List<T> options) {
        T currentSelection = comboBox.getItemCount() > 0 ? comboBox.getItemAt(comboBox.getSelectedIndex()) : null;

        comboBox.removeAllItems();
        if (options != null) {
            for (T option : options) {
                comboBox.addItem(option);
            }
        }

        if (currentSelection != null && options != null && options.contains(currentSelection)) {
            comboBox.setSelectedItem(currentSelection);
        }
    }

    private static <T> boolean areEqual(T a, T b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public static class BadgeListBinding {
        public Consumer<List<String>> listChangeListener;
    }

    public static BadgeListBinding setupBadgeListBinding(
            ComponentModel<List<String>> listModel,
            JPanel badgePanel,
            java.util.function.Function<String, JComponent> badgeFactory) {

        BadgeListBinding binding = new BadgeListBinding();

        binding.listChangeListener = items -> {
            SwingUtilities.invokeLater(() -> {
                badgePanel.removeAll();
                if (items != null) {
                    for (String item : items) {
                        badgePanel.add(badgeFactory.apply(item));
                    }
                }
                badgePanel.revalidate();
                badgePanel.repaint();
            });
        };
        listModel.addChangeListener(binding.listChangeListener);

        if (listModel.getValue() != null) {
            binding.listChangeListener.accept(listModel.getValue());
        }

        return binding;
    }

    public static void unbindBadgeList(
            ComponentModel<List<String>> listModel,
            Consumer<List<String>> listChangeListener) {

        if (listModel != null && listChangeListener != null) {
            listModel.removeChangeListener(listChangeListener);
        }
    }

    public static void addToBadgeList(ComponentModel<List<String>> listModel, String item) {
        List<String> current = listModel.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }
        if (!current.contains(item)) {
            List<String> updated = new ArrayList<>(current);
            updated.add(item);
            listModel.setValue(updated);
        }
    }

    public static void removeFromBadgeList(ComponentModel<List<String>> listModel, String item) {
        List<String> current = listModel.getValue();
        if (current != null && current.contains(item)) {
            List<String> updated = new ArrayList<>(current);
            updated.remove(item);
            listModel.setValue(updated);
        }
    }
}

