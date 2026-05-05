package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedBadge;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedLabel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewDetailModel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Panel displaying the header information for a code review context.
 * Shows review title, status badge, author, summary, and reviewer badges.
 * Reviewer badges are clickable to change reviewer status.
 * Includes edit and close review buttons that can be configured with custom actions.
 */
public class ReviewDetailPanel extends ThemedPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDetailPanel.class);

    private final ReviewDetailModel reviewDetailModel;

    private final ThemedBadge headerStatusBadge = new ThemedBadge("Status").setCustomColor(Color.DARK_GRAY);
    private final ThemedLabel titleLabel = new ThemedLabel("");
    private final ThemedButton editReviewButton = new ThemedButton("Edit");
    private final ThemedButton closeReviewButton = new ThemedButton("Close Review");

    private final ThemedLabel authorLabel = new ThemedLabel("author..");
    private final ThemedLabel summaryLabel = new ThemedLabel("summary..");

    private final ThemedPanel reviewerPanel = new ThemedPanel();

    private Runnable onEditAction;

    public ReviewDetailPanel(ReviewDetailModel reviewDetailModel) {
        this.reviewDetailModel = reviewDetailModel;

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        authorLabel.setFont(authorLabel.getFont().deriveFont(Font.ITALIC));

        configureLayout();
        configureReviewContextListeners();
        setupListeners();
        updateButtonStates();
    }

    private void configureLayout() {
        setLayout(new MigLayout("",
                "[]5lp[][grow][]", "[]8lp[]6lp[]10lp[]"));
        add(headerStatusBadge, "cell 0 0");
        add(titleLabel, "cell 1 0");
        add(editReviewButton, "cell 2 0, align right");
        add(closeReviewButton, "cell 3 0");

        add(authorLabel, "cell 0 1 2 1");
        add(summaryLabel, "cell 0 2 3 1");
        add(reviewerPanel, "cell 0 3 3 1");
    }

    private void configureReviewContextListeners() {

        reviewDetailModel.title.addChangeListener(val -> setLabelText(titleLabel, () -> val != null ? val : ""));
        reviewDetailModel.author.addChangeListener(val -> setLabelText(authorLabel, () -> val != null ? "Authored By " + val : ""));
        reviewDetailModel.summary.addChangeListener(val -> setLabelText(summaryLabel, () -> val != null ? val : ""));
        reviewDetailModel.status.addChangeListener(this::updateStatusBadge);

        reviewDetailModel.reviewers.addChangeListener(val -> {
            reviewerPanel.removeAll();
            if (val != null) {
                val.forEach(reviewer -> reviewerPanel.add(createReviewerBadge(reviewer)));
            }
            reviewerPanel.revalidate();
            reviewerPanel.repaint();
        });
    }

    private ThemedBadge createReviewerBadge(ReviewerInfo reviewer) {
        ThemedBadge badge = new ThemedBadge(
            reviewer.getName() + "  ·  " + reviewer.getStatus().getDisplayName());
        badge.setCustomColor(reviewer.getStatus().getColor());
        return badge;
    }

    private void updateStatusBadge(ReviewStatus status) {
        SwingUtilities.invokeLater(() -> {
            if (status != null) {
                headerStatusBadge.setText(status.toString());
                headerStatusBadge.setCustomColor(status.getColor());
            } else {
                headerStatusBadge.setText("Unknown");
                headerStatusBadge.setCustomColor(Color.DARK_GRAY);
            }
        });
    }

    private void updateButtonStates() {
        LOGGER.info("TODO: Implement updateButtonStates");
    }


    private void setupListeners() {
        editReviewButton.addActionListener(ignored -> handleEditReview());
        closeReviewButton.addActionListener(ignored -> handleCloseReview());
    }

    private void handleEditReview() {
        if (onEditAction != null) {
            onEditAction.run();
        } else {
            LOGGER.warn("Edit action not configured");
        }
    }

    private void handleCloseReview() {
        LOGGER.info("TODO: Implement close review action");
    }

    public void setOnEditAction(Runnable action) {
        this.onEditAction = action;
    }

    private void setLabelText(ThemedLabel label, Supplier<String> textSetter) {
        SwingUtilities.invokeLater(() -> {
            label.setText(textSetter.get());
        });
    }
}
