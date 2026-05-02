package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SourcePanel extends ThemedPanel {

    private static final int GAP = 12;
    private static final int FIELD_H = 28;

    private final Git git;
    private final ComponentModel<List<String>> selectedRepositoriesModel;
    private final ComponentModel<String> selectedBranchModel;
    private final ComponentModel<String> selectedBaseBranchModel;
    private final ComponentModel<List<String>> selectedCommitsModel;
    private final ThemedPanel modeSpecificPanel;
    private final ThemedSearchableComboBox branchNameField;
    private final ThemedSearchableComboBox reviewAgainstBranchCombo;
    private final ThemedSearchableComboBox commitBranchFilterCombo;
    private final ThemedList<String> commitSelectionList;
    private final DefaultListModel<String> commitListModel;

    public SourcePanel(ComponentModel<List<String>> availableBranchesModel,
                      ComponentModel<List<String>> selectedRepositoriesModel,
                      ComponentModel<String> selectedBranchModel,
                      ComponentModel<String> selectedBaseBranchModel,
                      ComponentModel<List<String>> selectedCommitsModel,
                      Git git) {
        this.git = git;
        this.selectedRepositoriesModel = selectedRepositoriesModel;
        this.selectedBranchModel = selectedBranchModel;
        this.selectedBaseBranchModel = selectedBaseBranchModel;
        this.selectedCommitsModel = selectedCommitsModel;

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

        commitListModel = new DefaultListModel<>();
        commitSelectionList = new ThemedList<>(commitListModel);
        commitSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        commitSelectionList.setVisibleRowCount(4);

        modeSpecificPanel = new ThemedPanel();
        modeSpecificPanel.setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));

        add(modeSpecificPanel, "grow");

        setupListeners();
    }

    private void setupListeners() {
        branchNameField.addActionListener(e -> {
            Object selected = branchNameField.getSelectedItem();
            if (selected != null) {
                selectedBranchModel.setValue(selected.toString());
            }
        });

        reviewAgainstBranchCombo.addActionListener(e -> {
            Object selected = reviewAgainstBranchCombo.getSelectedItem();
            if (selected != null) {
                selectedBaseBranchModel.setValue(selected.toString());
            }
        });

        commitBranchFilterCombo.addActionListener(e -> loadCommitsForSelectedBranch());

        commitSelectionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedCommitsModel.setValue(new ArrayList<>(commitSelectionList.getSelectedValuesList()));
            }
        });

        selectedRepositoriesModel.addChangeListener(ignored -> SwingUtilities.invokeLater(this::loadCommitsForSelectedBranch));
    }

    private void loadCommitsForSelectedBranch() {
        Object selectedBranch = commitBranchFilterCombo.getSelectedItem();
        List<String> selectedRepos = selectedRepositoriesModel.getValue();

        if (selectedBranch == null || selectedRepos == null || selectedRepos.isEmpty()) {
            commitListModel.clear();
            return;
        }

        String branch = selectedBranch.toString();
        if (branch.equals("All Branches")) {
            commitListModel.clear();
            return;
        }

        loadCommitsFromRepositories(selectedRepos, branch);
    }

    private void loadCommitsFromRepositories(List<String> repositories, String branch) {
        commitListModel.clear();

        List<CompletableFuture<List<String>>> commitFutures = repositories.stream()
            .map(repo -> git.listCommits(repo, "origin/" + branch, 50)
                .exceptionally(ex -> {
                    System.err.println("Failed to load commits for " + repo + " on branch " + branch + ": " + ex.getMessage());
                    return new ArrayList<>();
                }))
            .collect(Collectors.toList());

        CompletableFuture.allOf(commitFutures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> commitFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList()))
            .thenAccept(commits -> SwingUtilities.invokeLater(() -> {
                commitListModel.clear();
                for (String commit : commits) {
                    commitListModel.addElement(commit);
                }
            }))
            .exceptionally(ex -> {
                System.err.println("Failed to load commits: " + ex.getMessage());
                return null;
            });
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

