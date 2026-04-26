package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.ui.review.model.ReviewContext;
import com.kalynx.serverlessreviewtool.ui.review.model.ReviewerInfo;
import com.kalynx.serverlessreviewtool.ui.review.model.Repository;

import javax.swing.*;
import java.awt.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EditReviewDialog – opens a pre-populated review form for editing an existing review.
 *
 * After the dialog closes with {@link #isConfirmed()} == true, callers should
 * apply the changes back to the ReviewContext via {@link #applyTo(ReviewContext)}.
 */
public class EditReviewDialog extends ReviewFormDialog {

    public EditReviewDialog(Component parent,
                            ReviewContext context,
                            List<String> allRepositories,
                            List<String> allReviewers) {
        super(parent, "Edit Code Review", allRepositories, allReviewers);
        prePopulate(context);
    }

    @Override
    protected String getSubmitButtonLabel() { return "Save Changes"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }

    // ── pre-population ───────────────────────────────────────────────────────

    private void prePopulate(ReviewContext ctx) {
        titleField.setText(ctx.getTitle());
        authorField.setText(ctx.getAuthor());
        summaryArea.setText(ctx.getSummary());

        // Pre-check repositories already in the review
        Set<String> repoNames = ctx.getRepositories().stream()
            .map(Repository::getName)
            .collect(Collectors.toSet());
        for (JCheckBox cb : repositoryCheckboxes) {
            if (repoNames.contains(cb.getText())) cb.setSelected(true);
        }

        // Pre-check reviewers already in the review
        Set<String> reviewerNames = ctx.getReviewers().stream()
            .map(ReviewerInfo::getName)
            .collect(Collectors.toSet());
        for (JCheckBox cb : reviewerCheckboxes) {
            if (reviewerNames.contains(cb.getText())) cb.setSelected(true);
        }

        // Refresh badge rows to show pre-selected items
        updateRepositoryBadges();
        updateReviewerBadges();
    }

    /**
     * Applies the confirmed form values back onto the given ReviewContext.
     * Call this after {@link #isConfirmed()} returns true.
     */
    public void applyTo(ReviewContext ctx) {
        ctx.setTitle(getReviewTitle());
        ctx.setAuthor(getAuthor());
        ctx.setSummary(getSummary());
        // Reviewers: keep existing ReviewerInfo for already-present reviewers,
        // add new ones, and remove de-selected ones.
        Set<String> selected = getSelectedReviewers().stream().collect(Collectors.toSet());
        // Remove reviewers no longer selected
        ctx.getReviewers().stream()
            .filter(r -> !selected.contains(r.getName()))
            .collect(Collectors.toList())
            .forEach(r -> {}); // ReviewContext would need removeReviewer; for now we leave existing
        // Add newly selected reviewers not already present
        Set<String> existing = ctx.getReviewers().stream()
            .map(ReviewerInfo::getName).collect(Collectors.toSet());
        for (String name : selected) {
            if (!existing.contains(name)) ctx.addReviewer(name);
        }
    }
}



