package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewFormDialog;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EditReviewDialog extends ReviewFormDialog {

    private final ReviewContext originalContext;

    public EditReviewDialog(Component parent,
                            ReviewContext context,
                            List<String> allRepositories,
                            List<String> allReviewers) {
        super(parent, "Edit Code Review", allRepositories, allReviewers);
        this.originalContext = context;
        prePopulate(context);
    }

    @Override
    protected String getSubmitButtonLabel() { return "Save Changes"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }

    private void prePopulate(ReviewContext ctx) {
        titleField.setText(ctx.title);
        authorField.setText(ctx.author);
        summaryArea.setText(ctx.summary);

        Set<String> repoNames = ctx.repositories.stream()
            .map(Repository::getName)
            .collect(Collectors.toSet());
        for (JCheckBox cb : repositoryCheckboxes) {
            if (repoNames.contains(cb.getText())) cb.setSelected(true);
        }

        Set<String> reviewerNames = ctx.reviewers.stream()
            .map(ReviewerInfo::getName)
            .collect(Collectors.toSet());
        for (JCheckBox cb : reviewerCheckboxes) {
            if (reviewerNames.contains(cb.getText())) cb.setSelected(true);
        }

        updateRepositoryBadges();
        updateReviewerBadges();
    }

    public ReviewContext getUpdatedContext() {
        List<ReviewerInfo> updatedReviewers = new ArrayList<>(originalContext.reviewers);
        Set<String> selectedReviewerNames = new HashSet<>(getSelectedReviewers());
        
        updatedReviewers.removeIf(r -> !selectedReviewerNames.contains(r.getName()));
        
        Set<String> existingNames = updatedReviewers.stream()
            .map(ReviewerInfo::getName)
            .collect(Collectors.toSet());
        
        for (String name : selectedReviewerNames) {
            if (!existingNames.contains(name)) {
                updatedReviewers.add(new ReviewerInfo(name));
            }
        }

        List<Repository> updatedRepositories = new ArrayList<>();
        Set<String> selectedRepoNames = new HashSet<>(getSelectedRepositories());
        
        for (Repository repo : originalContext.repositories) {
            if (selectedRepoNames.contains(repo.getName())) {
                updatedRepositories.add(repo);
                selectedRepoNames.remove(repo.getName());
            }
        }
        
        for (String newRepoName : selectedRepoNames) {
            updatedRepositories.add(new Repository(newRepoName, "", ""));
        }

        return new ReviewContext(
            originalContext.reviewId,
            getReviewTitle(),
            getSummary(),
            getAuthor(),
            originalContext.status,
            updatedReviewers,
            updatedRepositories,
            originalContext.comments
        );
    }
}



