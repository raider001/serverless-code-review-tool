package com.kalynx.serverlessreviewtool.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * CreateReviewDialog – opens a blank review form for creating a new code review.
 */
public class CreateReviewDialog extends ReviewFormDialog {

    public CreateReviewDialog(Component parent, List<String> availableRepositories) {
        this(parent, availableRepositories, new ArrayList<>());
    }

    public CreateReviewDialog(Component parent,
                              List<String> availableRepositories,
                              List<String> availableReviewers) {
        super(parent, "Create Code Review", availableRepositories, availableReviewers);
    }

    @Override
    protected String getSubmitButtonLabel() { return "Create Review"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }
}
