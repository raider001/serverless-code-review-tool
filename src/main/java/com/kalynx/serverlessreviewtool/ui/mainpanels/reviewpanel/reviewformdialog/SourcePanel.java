package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SourcePanel extends ThemedPanel {

    private static final int GAP = 12;
    private static final int FIELD_H = 28;

    private final ThemedPanel modeSpecificPanel;
    private final ThemedSearchableComboBox branchNameField;
    private final ThemedSearchableComboBox reviewAgainstBranchCombo;
    private final ThemedSearchableComboBox commitBranchFilterCombo;
    private final ThemedList<String> commitSelectionList;

    public SourcePanel(ComponentModel<List<String>> availableBranchesModel) {
        setLayout(new MigLayout("fill, insets 10 12 12 12", "[grow,fill]", "[grow,fill]"));
        setBorder(ThemedTitledBorder.create("Source"));

        branchNameField = new ThemedSearchableComboBox(new ArrayList<>());
        branchNameField.setToolTipText("Search for a branch to review");
        branchNameField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));
        branchNameField.bindTo(availableBranchesModel);

        reviewAgainstBranchCombo = new ThemedSearchableComboBox(new ArrayList<>());
        reviewAgainstBranchCombo.setToolTipText("Search for a branch to review against");
        reviewAgainstBranchCombo.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));
        reviewAgainstBranchCombo.bindTo(availableBranchesModel);

        commitBranchFilterCombo = new ThemedSearchableComboBox(new ArrayList<>());
        commitBranchFilterCombo.setToolTipText("Search for a branch to filter commits");
        commitBranchFilterCombo.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));
        commitBranchFilterCombo.bindTo(availableBranchesModel);

        DefaultListModel<String> commitListModel = new DefaultListModel<>();
        for (String c : new String[]{
            "abc1234 - Fix authentication issue",
            "def5678 - Add dark mode support",
            "ghi9012 - Update dependencies",
            "jkl3456 - Refactor UI components",
            "mno7890 - Optimize database queries"}) {
            commitListModel.addElement(c);
        }

        commitSelectionList = new ThemedList<>(commitListModel);
        commitSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        commitSelectionList.setVisibleRowCount(4);

        modeSpecificPanel = new ThemedPanel();
        modeSpecificPanel.setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));

        add(modeSpecificPanel, "grow");
    }

    public void updateMode(boolean branchMode) {
        modeSpecificPanel.removeAll();
        if (branchMode) {
            modeSpecificPanel.add(createBranchModePanel(), "grow");
        } else {
            modeSpecificPanel.add(createCommitModePanel(), "grow");
        }
        modeSpecificPanel.revalidate();
        modeSpecificPanel.repaint();
    }

    private ThemedPanel createBranchModePanel() {
        ThemedPanel panel = new ThemedPanel();
        panel.setLayout(new MigLayout(
            "insets 0, gap " + GAP + " 8",
            "[100!][grow,fill]" + GAP + "[110!][grow,fill]",
            "[]"
        ));

        panel.add(rightLabel("Branch:"));
        panel.add(branchNameField, "growx");
        panel.add(rightLabel("Review against:"));
        panel.add(reviewAgainstBranchCombo, "growx");
        return panel;
    }

    private ThemedPanel createCommitModePanel() {
        ThemedPanel panel = new ThemedPanel();
        panel.setLayout(new MigLayout(
            "fill, insets 0, gap " + GAP + " 8",
            "[100!][grow,fill]",
            "[]8[][grow,fill]"
        ));

        panel.add(rightLabel("Filter by branch:"));
        panel.add(commitBranchFilterCombo, "growx, wrap");

        ThemedScrollPane scroll = new ThemedScrollPane(commitSelectionList);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(rightLabel("Commits:"), "aligny top, gaptop 4");
        panel.add(scroll, "grow");
        return panel;
    }

    private ThemedLabel rightLabel(String text) {
        ThemedLabel label = new ThemedLabel(text);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    public String getBranchName() {
        Object selected = branchNameField.getSelectedItem();
        return selected != null ? selected.toString() : "";
    }

    public String getReviewAgainstBranch() {
        return (String) reviewAgainstBranchCombo.getSelectedItem();
    }

    public String getSelectedCommit() {
        return commitSelectionList.getSelectedValue();
    }

    public List<String> getSelectedCommits() {
        return new ArrayList<>(commitSelectionList.getSelectedValuesList());
    }

    public String getSelectedBranchFilter() {
        return (String) commitBranchFilterCombo.getSelectedItem();
    }

    public void setBranchName(String branchName) {
        branchNameField.setSelectedItem(branchName);
    }

    public void setReviewAgainstBranch(String branch) {
        reviewAgainstBranchCombo.setSelectedItem(branch);
    }

    public void setCommitBranchFilter(String branchFilter) {
        commitBranchFilterCombo.setSelectedItem(branchFilter);
    }

    public void setSelectedCommits(List<String> commits) {
        if (commits == null || commits.isEmpty()) {
            commitSelectionList.clearSelection();
            return;
        }

        DefaultListModel<String> model = (DefaultListModel<String>) commitSelectionList.getModel();
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < model.getSize(); i++) {
            String commit = model.getElementAt(i);
            if (commits.contains(commit)) {
                indices.add(i);
            }
        }

        int[] selectedIndices = indices.stream().mapToInt(Integer::intValue).toArray();
        commitSelectionList.setSelectedIndices(selectedIndices);
    }

    public boolean hasCommitSelection() {
        return !commitSelectionList.getSelectedValuesList().isEmpty();
    }
}

