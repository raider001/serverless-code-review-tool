package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog.*;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ReviewFormDialog extends ThemedPopupDialog {

    private static final int DIALOG_W = 880;
    private static final int DIALOG_H = 720;
    private static final int GAP = 12;
    private static final int INSET = 16;

    protected final ThemeManager themeManager;
    protected final ReviewFormModels models;
    protected final RepositoryManager repositoryManager;
    protected boolean confirmed = false;

    protected final ReviewDetailsPanel detailsPanel;
    protected final SourcePanel sourcePanel;
    protected final RepositoriesPanel repositoriesPanel;
    protected final ReviewersPanel reviewersPanel;

    protected ReviewFormDialog(Component parent,
                                String dialogTitle,
                                ReviewFormModels models,
                                RepositoryManager repositoryManager) {
        super(parent, dialogTitle);
        this.themeManager = ThemeManager.getInstance();
        this.models = models;
        this.repositoryManager = repositoryManager;

        this.detailsPanel = new ReviewDetailsPanel(
            models.title,
            models.author,
            models.summary,
            models.mode
        );
        this.sourcePanel = new SourcePanel(models.availableBranches);
        this.repositoriesPanel = new RepositoriesPanel(
            models.availableRepositories,
            models.selectedRepositories
        );
        this.reviewersPanel = new ReviewersPanel(
            models.availableReviewers,
            models.selectedReviewers
        );

        setDialogSize(themeManager.scale(DIALOG_W), themeManager.scale(DIALOG_H));
        setUserResizable(true);

        configureLayout();
        setupListeners();

        if (parent != null) {
            Point p = parent.getLocationOnScreen();
            setLocation(
                p.x + parent.getWidth() / 2 - getWidth() / 2,
                p.y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
    }

    private void configureLayout() {
        ThemedPanel content = (ThemedPanel) getContentPanel();
        content.setLayout(new MigLayout(
            "fill, insets " + INSET + ", gap " + GAP + " " + GAP,
            "[grow]",
            "[]" + GAP + "[]" + GAP + "[]" + GAP + "[]"
        ));

        content.add(detailsPanel, "grow, wrap");
        content.add(createSelectionSection(), "grow, wrap");
        content.add(sourcePanel, "grow, wrap");
        content.add(createFooter(), "growx");

        sourcePanel.updateMode(detailsPanel.isBranchMode());
    }

    private void setupListeners() {
        detailsPanel.setOnModeChangeListener(() -> sourcePanel.updateMode(detailsPanel.isBranchMode()));

        models.selectedRepositories.addChangeListener(this::updateAvailableBranches);
        updateAvailableBranches(models.selectedRepositories.getValue());
    }

    private void updateAvailableBranches(List<String> selectedRepoNames) {
        if (selectedRepoNames == null || selectedRepoNames.isEmpty()) {
            models.availableBranches.setValue(new ArrayList<>());
            return;
        }

        List<Repository> allRepos = repositoryManager.getRepositories();
        List<Repository> selectedRepos = selectedRepoNames.stream()
            .map(name -> findRepositoryByName(allRepos, name))
            .filter(repo -> repo != null)
            .collect(Collectors.toList());

        if (selectedRepos.isEmpty()) {
            models.availableBranches.setValue(new ArrayList<>());
            return;
        }

        List<String> commonBranches = findCommonBranches(selectedRepos);
        models.availableBranches.setValue(commonBranches);
    }

    private Repository findRepositoryByName(List<Repository> repositories, String name) {
        return repositories.stream()
            .filter(repo -> repo.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private List<String> findCommonBranches(List<Repository> repositories) {
        if (repositories.isEmpty()) {
            return new ArrayList<>();
        }

        if (repositories.size() == 1) {
            return new ArrayList<>(repositories.get(0).getBranches());
        }

        List<String> commonBranches = new ArrayList<>(repositories.get(0).getBranches());

        for (int i = 1; i < repositories.size(); i++) {
            List<String> repoBranches = repositories.get(i).getBranches();
            commonBranches.retainAll(repoBranches);
        }

        return commonBranches;
    }

    private ThemedPanel createSelectionSection() {
        ThemedPanel row = new ThemedPanel();
        row.setLayout(new MigLayout(
            "fill, insets 0, gap " + GAP + " 0",
            "[grow,fill,sg cols]" + GAP + "[grow,fill,sg cols]",
            "[grow,fill]"
        ));

        row.add(repositoriesPanel, "grow");
        row.add(reviewersPanel, "grow");

        return row;
    }

    private ThemedPanel createFooter() {
        ThemedPanel footer = new ThemedPanel();
        footer.setLayout(new MigLayout("insets 0, gap 8", "[grow,fill][][]", "[]"));
        footer.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(themeManager.getCurrentTheme().getBorderColor());

        ThemedButton cancelBtn = new ThemedButton("Cancel");
        cancelBtn.addActionListener(ignored -> dispose());

        ThemedButton submitBtn = new ThemedButton(getSubmitButtonLabel());
        submitBtn.setAccentStyle(true);
        submitBtn.addActionListener(ignored -> handleSubmit());

        footer.add(sep, "growx, wrap, gapbottom 10, span");
        footer.add(Box.createGlue(), "growx");
        footer.add(cancelBtn);
        footer.add(submitBtn);
        return footer;
    }


    private void handleSubmit() {
        if (detailsPanel.getTitle().trim().isEmpty()) {
            warn("Please enter a title for the review");
            return;
        }
        if (detailsPanel.isBranchMode()) {
            if (sourcePanel.getBranchName().trim().isEmpty()) {
                warn("Please enter a branch name to review");
                return;
            }
            if (sourcePanel.getReviewAgainstBranch() == null) {
                warn("Please select a branch to review against");
                return;
            }
        } else if (!sourcePanel.hasCommitSelection()) {
            warn("Please select at least one commit");
            return;
        }

        List<String> reviewers = models.selectedReviewers.getValue();
        if (reviewers == null || reviewers.isEmpty()) {
            warn("Please select at least one reviewer");
            return;
        }

        onFormSubmit();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isBranchMode() {
        return detailsPanel.isBranchMode();
    }

    public String getReviewTitle() {
        return detailsPanel.getTitle();
    }

    public String getAuthor() {
        return detailsPanel.getAuthor();
    }

    public String getSummary() {
        return detailsPanel.getSummary();
    }

    public String getBranchName() {
        return sourcePanel.getBranchName();
    }

    public String getReviewAgainstBranch() {
        return sourcePanel.getReviewAgainstBranch();
    }

    public String getSelectedCommit() {
        return sourcePanel.getSelectedCommit();
    }

    public List<String> getSelectedCommits() {
        return sourcePanel.getSelectedCommits();
    }

    public String getSelectedBranchFilter() {
        return sourcePanel.getSelectedBranchFilter();
    }

    protected abstract String getSubmitButtonLabel();

    protected abstract void onFormSubmit();
}