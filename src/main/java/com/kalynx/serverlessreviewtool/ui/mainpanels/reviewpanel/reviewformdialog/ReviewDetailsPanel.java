package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
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

    public ReviewDetailsPanel() {
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
        branchModeRadio.addActionListener(e -> listener.run());
        commitModeRadio.addActionListener(e -> listener.run());
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

    public void setTitle(String title) {
        titleField.setText(title);
    }

    public void setAuthor(String author) {
        authorField.setText(author);
    }

    public void setSummary(String summary) {
        summaryArea.setText(summary);
    }

    public void setBranchMode(boolean branchMode) {
        if (branchMode) {
            branchModeRadio.setSelected(true);
        } else {
            commitModeRadio.setSelected(true);
        }
    }
}

