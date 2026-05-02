package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.ReviewFormDialog;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;

import java.awt.Component;

public class CreateReviewDialog extends ReviewFormDialog {

    public CreateReviewDialog(Component parent,
                             ReviewFormModels models,
                             RepositoryManager repositoryManager,
                             Git git) {
        super(parent, "Create Code Review", models, repositoryManager, git);
        models.clear();
    }

    @Override
    protected String getSubmitButtonLabel() { return "Create Review"; }

    @Override
    protected void onFormSubmit() { confirmed = true; dispose(); }
}
