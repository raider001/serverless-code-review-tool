package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import java.util.ArrayList;
import java.util.List;

public class ReviewFormValidator {

    public static ValidationResult validate(ReviewFormModels models) {
        return validate(models, models.availableReviewers.getValue());
    }

    public static ValidationResult validate(ReviewFormModels models, List<String> validAuthors) {
        List<String> errors = new ArrayList<>();

        if (models.title.getValue() == null || models.title.getValue().trim().isEmpty()) {
            errors.add("Please enter a title for the review");
        }

        if (models.author.getValue() == null || models.author.getValue().trim().isEmpty()) {
            errors.add("Please enter an author name");
        } else if (validAuthors != null && !validAuthors.isEmpty()) {
            String author = models.author.getValue().trim();
            boolean isValidAuthor = validAuthors.stream().anyMatch(author::equals);
            if (!isValidAuthor) {
                errors.add("Please enter a valid author from the known users list");
            }
        }

        if (models.selectedBranchModel.getValue() == null || models.selectedBranchModel.getValue().trim().isEmpty()) {
            errors.add("Please enter a branch name to review");
        }

        if (models.selectedBaseBranchModel.getValue() == null || models.selectedBaseBranchModel.getValue().trim().isEmpty()) {
            errors.add("Please select a branch to review against");
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

        public String getAllErrors() {
            return String.join("\n", errors);
        }
    }
}

