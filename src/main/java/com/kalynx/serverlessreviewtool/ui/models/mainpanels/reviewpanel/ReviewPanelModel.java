package com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;

public class ReviewPanelModel {

    public final ComponentModel<String> currentReviewId = new ComponentModel<>();
    public final ComponentModel<Boolean> hasActiveReview = new ComponentModel<>();
    public final ComponentModel<String> errorMessage = new ComponentModel<>();

    public final CodeViewerModel codeViewerModel;
    public final CommentsPanelModel commentsPanelModel;
    public final ReviewActionModel reviewActionModel;

    public ReviewPanelModel() {
        this.codeViewerModel = new CodeViewerModel();
        this.commentsPanelModel = new CommentsPanelModel();
        this.reviewActionModel = new ReviewActionModel();
        initializeDefaults();
    }

    private void initializeDefaults() {
        currentReviewId.setValue(null);
        hasActiveReview.setValue(false);
        errorMessage.setValue("");
    }

    public void setCurrentReview(String reviewId) {
        currentReviewId.setValue(reviewId);
        hasActiveReview.setValue(reviewId != null && !reviewId.isEmpty());
        if (reviewId == null || reviewId.isEmpty()) {
            clear();
        }
    }

    public void clear() {
        currentReviewId.setValue(null);
        hasActiveReview.setValue(false);
        errorMessage.setValue("");
        codeViewerModel.clear();
        commentsPanelModel.clear();
        reviewActionModel.clear();
    }

    public void setError(String error) {
        errorMessage.setValue(error != null ? error : "");
    }
}

