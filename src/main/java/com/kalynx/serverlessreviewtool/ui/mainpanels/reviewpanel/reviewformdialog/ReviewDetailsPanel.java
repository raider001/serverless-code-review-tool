package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ReviewDetailsPanel extends ThemedPanel {

    private static final int GAP = 12;
    private static final int FIELD_H = 28;
    private static final int SUMMARY_H = 120;

    private final ThemeManager themeManager;
    private final ThemedRadioButton branchModeRadio;
    private final ThemedRadioButton commitModeRadio;
    private final ThemedTextField titleField;
    private final ThemedTextField authorField;
    private final ThemedTextArea summaryArea;

    private boolean updatingFromModel = false;

    public ReviewDetailsPanel(ComponentModel<String> titleModel,
                              ComponentModel<String> authorModel,
                              ComponentModel<String> summaryModel,
                              ComponentModel<ReviewFormModels.ReviewMode> modeModel) {
        this.themeManager = ThemeManager.getInstance();

        setLayout(new MigLayout(
            "insets 10 12 12 12, gap " + GAP + " 8",
            "[90!][grow,fill][]",
            "[]8[]8[]"
        ));
        setBorder(ThemedTitledBorder.create("Review Details"));

        branchModeRadio = new ThemedRadioButton("Branch");
        commitModeRadio = new ThemedRadioButton("Commit");
        branchModeRadio.setSelected(true);

        ButtonGroup grp = new ButtonGroup();
        grp.add(branchModeRadio);
        grp.add(commitModeRadio);

        titleField = new ThemedTextField(20);
        titleField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        authorField = new ThemedTextField(20);
        authorField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));
        authorField.setToolTipText("Name of the review author");

        summaryArea = new ThemedTextArea(3, 40);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        configureLayout();
        bindToModels(titleModel, authorModel, summaryModel, modeModel);
    }

    private void bindToModels(ComponentModel<String> titleModel,
                              ComponentModel<String> authorModel,
                              ComponentModel<String> summaryModel,
                              ComponentModel<ReviewFormModels.ReviewMode> modeModel) {
        titleModel.addChangeListener(value -> {
            SwingUtilities.invokeLater(() -> {
                updatingFromModel = true;
                titleField.setText(value != null ? value : "");
                updatingFromModel = false;
            });
        });

        authorModel.addChangeListener(value -> {
            SwingUtilities.invokeLater(() -> {
                updatingFromModel = true;
                authorField.setText(value != null ? value : "");
                updatingFromModel = false;
            });
        });

        summaryModel.addChangeListener(value -> {
            SwingUtilities.invokeLater(() -> {
                updatingFromModel = true;
                summaryArea.setText(value != null ? value : "");
                updatingFromModel = false;
            });
        });

        modeModel.addChangeListener(mode -> {
            SwingUtilities.invokeLater(() -> {
                updatingFromModel = true;
                if (mode == ReviewFormModels.ReviewMode.BRANCH) {
                    branchModeRadio.setSelected(true);
                } else {
                    commitModeRadio.setSelected(true);
                }
                updatingFromModel = false;
            });
        });

        titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            private void updateModel() {
                if (!updatingFromModel) {
                    titleModel.setValue(titleField.getText());
                }
            }
        });

        authorField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            private void updateModel() {
                if (!updatingFromModel) {
                    authorModel.setValue(authorField.getText());
                }
            }
        });

        summaryArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            private void updateModel() {
                if (!updatingFromModel) {
                    summaryModel.setValue(summaryArea.getText());
                }
            }
        });

        branchModeRadio.addActionListener(e -> {
            if (!updatingFromModel) {
                modeModel.setValue(ReviewFormModels.ReviewMode.BRANCH);
            }
        });

        commitModeRadio.addActionListener(e -> {
            if (!updatingFromModel) {
                modeModel.setValue(ReviewFormModels.ReviewMode.COMMIT);
            }
        });

        if (titleModel.getValue() != null) titleField.setText(titleModel.getValue());
        if (authorModel.getValue() != null) authorField.setText(authorModel.getValue());
        if (summaryModel.getValue() != null) summaryArea.setText(summaryModel.getValue());
        if (modeModel.getValue() == ReviewFormModels.ReviewMode.COMMIT) {
            commitModeRadio.setSelected(true);
        }
    }

    private void configureLayout() {
        ThemedPanel modeToggle = new ThemedPanel();
        modeToggle.setLayout(new MigLayout("insets 0, gap 4", "[][]", ""));
        modeToggle.setOpaque(false);
        modeToggle.add(new ThemedLabel("Mode:"));
        modeToggle.add(branchModeRadio);
        modeToggle.add(commitModeRadio);

        add(rightLabel("Title:"));
        add(titleField, "growx");
        add(modeToggle, "wrap");

        add(rightLabel("Author:"));
        add(authorField, "growx, span 2, wrap");

        ThemedScrollPane summaryScroll = new ThemedScrollPane(summaryArea);
        summaryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        summaryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setPreferredSize(new Dimension(0, themeManager.scale(SUMMARY_H)));

        add(rightLabel("Summary:"), "aligny top, gaptop 4");
        add(summaryScroll, "grow, span 2");
    }

    private ThemedLabel rightLabel(String text) {
        ThemedLabel label = new ThemedLabel(text);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    public void setOnModeChangeListener(Runnable listener) {
        branchModeRadio.addActionListener(ignored -> listener.run());
        commitModeRadio.addActionListener(ignored -> listener.run());
    }

    public boolean isBranchMode() {
        return branchModeRadio.isSelected();
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getAuthor() {
        return authorField.getText();
    }

    public String getSummary() {
        return summaryArea.getText();
    }
}

