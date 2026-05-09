package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog.*;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ReviewFormDialog extends ThemedPopupDialog {

    private static final int DIALOG_W = 880;
    private static final int DIALOG_H = 720;
    private static final int GAP = 12;
    private static final int INSET = 16;

    protected final ThemeManager themeManager;
    protected final ReviewFormModels models;
    protected final RepositoryManager repositoryManager;
    protected final Git git;
    protected boolean confirmed = false;

    protected final ReviewDetailsPanel detailsPanel;
    protected final SourcePanel sourcePanel;
    protected final RepositoriesPanel repositoriesPanel;
    protected final ReviewersPanel reviewersPanel;

    protected ReviewFormDialog(Component parent,
                                String dialogTitle,
                                ReviewFormModels models,
                                RepositoryManager repositoryManager,
                                Git git) {
        super(parent, dialogTitle);
        this.themeManager = ThemeManager.getInstance();
        this.models = models;
        this.repositoryManager = repositoryManager;
        this.git = git;

        this.detailsPanel = new ReviewDetailsPanel(
            models.title,
            models.author,
            models.summary
        );
        this.sourcePanel = new SourcePanel(
            models.availableBranches,
            models.selectedBranchModel,
            models.selectedBaseBranchModel
        );
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

        if (shouldShowFooter()) {
            content.add(createFooter(), "growx");
        }
    }

    protected boolean shouldShowFooter() {
        return true;
    }

    private void setupListeners() {
        models.selectedRepositories.addChangeListener(repos -> SwingUtilities.invokeLater(() -> updateAvailableBranches(repos)));
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
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (selectedRepos.isEmpty()) {
            models.availableBranches.setValue(new ArrayList<>());
            return;
        }

        String primaryRepoName = selectedRepoNames.getFirst();
        git.fetch(primaryRepoName)
            .thenCompose(ignored -> git.listBranches(primaryRepoName))
            .thenAccept(branches -> SwingUtilities.invokeLater(() -> {
                repositoryManager.updateBranchesForRepository(primaryRepoName, branches);
                List<Repository> updatedRepos = repositoryManager.getRepositories();
                List<Repository> updatedSelectedRepos = selectedRepoNames.stream()
                    .map(name -> findRepositoryByName(updatedRepos, name))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                List<String> commonBranches = findCommonBranches(updatedSelectedRepos);
                models.availableBranches.setValue(commonBranches);
            }))
            .exceptionally(error -> {
                System.err.println("Failed to fetch branches: " + error.getMessage());
                List<String> commonBranches = findCommonBranches(selectedRepos);
                SwingUtilities.invokeLater(() -> models.availableBranches.setValue(commonBranches));
                return null;
            });
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
            return new ArrayList<>(repositories.getFirst().getBranches());
        }

        List<String> commonBranches = new ArrayList<>(repositories.getFirst().getBranches());

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
        printReviewFormModels();

        if (detailsPanel.getTitle().trim().isEmpty()) {
            warn("Please enter a title for the review");
            return;
        }

        if (isValidAuthor(detailsPanel.getAuthor())) {
            warn(getInvalidAuthorMessage());
            return;
        }

        if (sourcePanel.getBranchName().trim().isEmpty()) {
            warn("Please enter a branch name to review");
            return;
        }

        if (sourcePanel.getReviewAgainstBranch() == null) {
            warn("Please select a branch to review against");
            return;
        }

        List<ReviewerInfo> reviewers = models.selectedReviewers.getValue();
        if (reviewers == null || reviewers.isEmpty()) {
            warn("Please select at least one reviewer");
            return;
        }

        onFormSubmit();
    }

    private void warn(String msg) {
        ThemedOptionPane.showWarning(this, msg);
    }

    private void printReviewFormModels() {
        System.out.println("=== ReviewFormModels Values ===");
        System.out.println("Review ID: " + models.reviewId.getValue());
        System.out.println("Title: " + models.title.getValue());
        System.out.println("Author: " + models.author.getValue());
        System.out.println("Summary: " + models.summary.getValue());
        System.out.println("Selected Branch: " + models.selectedBranchModel.getValue());
        System.out.println("Selected Base Branch: " + models.selectedBaseBranchModel.getValue());
        System.out.println("Available Branches: " + models.availableBranches.getValue());
        System.out.println("Selected Repositories: " + models.selectedRepositories.getValue());
        System.out.println("Available Repositories: " + models.availableRepositories.getValue());
        System.out.println("Selected Reviewers: " + models.selectedReviewers.getValue());
        System.out.println("Available Reviewers: " + models.availableReviewers.getValue());
        System.out.println("===============================");
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getAuthor() {
        return detailsPanel.getAuthor();
    }

    public String getSummary() {
        return detailsPanel.getSummary();
    }

    protected boolean isValidAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return true;
        }

        List<String> validAuthors = models.availableReviewers.getValue();
        if (validAuthors == null || validAuthors.isEmpty()) {
            return false;
        }

        String normalizedAuthor = author.trim();
        return validAuthors.stream().noneMatch(normalizedAuthor::equals);
    }

    protected String getInvalidAuthorMessage() {
        List<String> validAuthors = models.availableReviewers.getValue();
        if (validAuthors == null || validAuthors.isEmpty()) {
            return "Please enter an author name";
        }
        return "Please enter a valid author from the known users list";
    }

    protected abstract String getSubmitButtonLabel();

    protected abstract void onFormSubmit();
}
