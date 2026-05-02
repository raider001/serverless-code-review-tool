package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewFormDialog;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;

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
                            ReviewFormModels models,
                            RepositoryManager repositoryManager,
                            Git git) {
        super(parent, "Edit Code Review", models, repositoryManager, git);
        this.originalContext = context;
        populateModelsFromContext(context);
    }

    @Override
    protected String getSubmitButtonLabel() { return "Save Changes"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }

    private void populateModelsFromContext(ReviewContext ctx) {
        models.title.setValue(ctx.title);
        models.author.setValue(ctx.author);
        models.summary.setValue(ctx.summary);

        List<String> repoNames = ctx.repositories.stream()
            .map(Repository::getName)
            .collect(Collectors.toList());
        models.selectedRepositories.setValue(repoNames);

        List<String> reviewerNames = ctx.reviewers.stream()
            .map(ReviewerInfo::getName)
            .collect(Collectors.toList());
        models.selectedReviewers.setValue(reviewerNames);
    }

    public ReviewContext getUpdatedContext() {
        List<ReviewerInfo> updatedReviewers = new ArrayList<>(originalContext.reviewers);
        Set<String> selectedReviewerNames = new HashSet<>(models.selectedReviewers.getValue());

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
        Set<String> selectedRepoNames = new HashSet<>(models.selectedRepositories.getValue());

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
            models.title.getValue(),
            models.summary.getValue(),
            models.author.getValue(),
            originalContext.status,
            updatedReviewers,
            updatedRepositories,
            originalContext.comments
        );
    }
}



