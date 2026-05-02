package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormValidator {

    public static ValidationResult validate(ReviewFormModel model) {
        List<String> errors = new ArrayList<>();

        if (model.getTitle().trim().isEmpty()) {
            errors.add("Please enter a title for the review");
        }

        if (model.getAuthor().trim().isEmpty()) {
            errors.add("Please enter an author name");
        }

        if (model.isBranchMode()) {
            if (model.getBranchName().trim().isEmpty()) {
                errors.add("Please enter a branch name to review");
            }
            if (model.getReviewAgainstBranch() == null || model.getReviewAgainstBranch().trim().isEmpty()) {
                errors.add("Please select a branch to review against");
            }
        } else {
            if (!model.hasSelectedCommits()) {
                errors.add("Please select at least one commit");
            }
        }

        if (!model.hasSelectedRepositories()) {
            errors.add("Please select at least one repository");
        }

        if (!model.hasSelectedReviewers()) {
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

