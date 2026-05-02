package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormValidator {

    public static ValidationResult validate(ReviewFormModels models) {
        List<String> errors = new ArrayList<>();

        if (models.title.getValue() == null || models.title.getValue().trim().isEmpty()) {
            errors.add("Please enter a title for the review");
        }

        if (models.author.getValue() == null || models.author.getValue().trim().isEmpty()) {
            errors.add("Please enter an author name");
        }

        if (models.mode.getValue() == ReviewFormModels.ReviewMode.BRANCH) {
            if (models.branchName.getValue() == null || models.branchName.getValue().trim().isEmpty()) {
                errors.add("Please enter a branch name to review");
            }
            if (models.reviewAgainstBranch.getValue() == null || models.reviewAgainstBranch.getValue().trim().isEmpty()) {
                errors.add("Please select a branch to review against");
            }
        } else {
            if (models.selectedCommits.getValue() == null || models.selectedCommits.getValue().isEmpty()) {
                errors.add("Please select at least one commit");
            }
        }

        if (models.selectedRepositories.getValue() == null || models.selectedRepositories.getValue().isEmpty()) {
            errors.add("Please select at least one repository");
        }

        if (models.selectedReviewers.getValue() == null || models.selectedReviewers.getValue().isEmpty()) {
            errors.add("Please select at least one reviewer");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getFirstError() {
            return errors.isEmpty() ? "" : errors.get(0);
        }

        public String getAllErrors() {
            return String.join("\n", errors);
        }
    }
}

