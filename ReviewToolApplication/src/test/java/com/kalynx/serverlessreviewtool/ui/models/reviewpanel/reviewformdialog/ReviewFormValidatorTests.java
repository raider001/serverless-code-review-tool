package com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests author validation rules for review form submission.
 */
class ReviewFormValidatorTests {

    @Test
    void validate_whenAuthorIsKnown_returnsValid() {
        ReviewFormModels models = createValidModels();
        models.author.setValue("Captain Bugbeard");
        models.availableReviewers.setValue(List.of("Captain Bugbeard", "Null Pointerella"));

        ReviewFormValidator.ValidationResult result = ReviewFormValidator.validate(models);

        assertTrue(result.isValid());
    }

    @Test
    void validate_whenAuthorIsUnknownAndKnownUsersExist_returnsInvalid() {
        ReviewFormModels models = createValidModels();
        models.author.setValue("Unknown Human");
        models.availableReviewers.setValue(List.of("Captain Bugbeard", "Null Pointerella"));

        ReviewFormValidator.ValidationResult result = ReviewFormValidator.validate(models);

        assertFalse(result.isValid());
        assertTrue(result.getAllErrors().contains("valid author"));
    }

    @Test
    void validate_whenKnownUsersDoNotExistAndAuthorIsNonEmpty_returnsValid() {
        ReviewFormModels models = createValidModels();
        models.author.setValue("Offline Author");
        models.availableReviewers.setValue(List.of());

        ReviewFormValidator.ValidationResult result = ReviewFormValidator.validate(models);

        assertTrue(result.isValid());
    }

    private ReviewFormModels createValidModels() {
        ReviewFormModels models = new ReviewFormModels();
        models.title.setValue("A valid title");
        models.author.setValue("Captain Bugbeard");
        models.selectedBranchModel.setValue("feature/login");
        models.selectedBaseBranchModel.setValue("main");
        models.selectedRepositories.setValue(List.of("java-backend-service"));
        models.selectedReviewers.setValue(List.of(new ReviewerInfo("Null Pointerella")));
        return models;
    }
}


