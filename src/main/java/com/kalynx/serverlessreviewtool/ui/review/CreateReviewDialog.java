package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewFormDialog;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;

import java.awt.Component;

/**
 * CreateReviewDialog – opens a blank review form for creating a new code review.
 */
public class CreateReviewDialog extends ReviewFormDialog {

    public CreateReviewDialog(Component parent, ReviewFormModels models) {
        super(parent, "Create Code Review", models);
    }

    @Override
    protected String getSubmitButtonLabel() { return "Create Review"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }
}
