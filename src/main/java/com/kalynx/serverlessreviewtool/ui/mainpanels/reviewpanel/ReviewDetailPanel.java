package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.ReviewerInfo;
import com.kalynx.serverlessreviewtool.models.ReviewerStatus;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedBadge;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedLabel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedMenuItem;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPopupMenu;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import com.kalynx.serverlessreviewtool.ui.review.EditReviewDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

/**
 * Panel displaying the header information for a code review context.
 * Shows review title, status badge, author, summary, and reviewer badges.
 * Reviewer badges are clickable to change reviewer status.
 * Includes edit and close review buttons that can be configured with custom actions.
 */
public class ReviewDetailPanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager;
    private final ReviewFormModels reviewFormModels;

    private final ThemedBadge headerStatusBadge = new ThemedBadge("Status").setCustomColor(Color.DARK_GRAY);
    private final ThemedLabel titleLabel = new ThemedLabel("");
    private final ThemedButton editReviewButton = new ThemedButton("Edit");
    private final ThemedButton closeReviewButton = new ThemedButton("Close Review");

    private final ThemedLabel authorLabel = new ThemedLabel("author..");
    private final ThemedLabel summaryLabel = new ThemedLabel("summary..");

    private final ThemedPanel reviewerPanel = new ThemedPanel();

    public ReviewDetailPanel(ReviewContextManager reviewContextManager, ReviewFormModels reviewFormModels) {
        this.reviewContextManager = reviewContextManager;
        this.reviewFormModels = reviewFormModels;
        configureLayout();
        configureReviewContextListeners();
        setupListeners();
        updateButtonStates();
    }

    private void configureLayout() {
        setLayout(new MigLayout("",
                "[][][grow][]", "[]8lp[]6lp[]10lp[]"));
        add(headerStatusBadge, "cell 0 0");
        add(titleLabel, "cell 1 0");
        add(editReviewButton, "cell 2 0, align right");
        add(closeReviewButton, "cell 3 0");

        add(authorLabel, "cell 0 1");
        add(summaryLabel, "cell 0 2 3 1");
        add(reviewerPanel, "cell 0 3 3 1");
    }

    private void configureReviewContextListeners() {
        reviewContextManager.addListener(context -> setLabelText(titleLabel, () -> context.title));
        reviewContextManager.addListener(context -> setLabelText(authorLabel, () -> context.author));
        reviewContextManager.addListener(context -> setLabelText(summaryLabel, () -> context.summary));
        reviewContextManager.addListener(this::updateStatusBadge);
        reviewContextManager.addListener(this::updateReviewers);
        reviewContextManager.addListener(ignored -> updateButtonStates());
    }

    private void updateReviewers(ReviewContext context) {
        reviewerPanel.removeAll();
        if (context != null) {
            context.reviewers.forEach(reviewer -> reviewerPanel.add(createReviewerBadge(reviewer)));
        }
        reviewerPanel.revalidate();
        reviewerPanel.repaint();
    }

    private ThemedBadge createReviewerBadge(ReviewerInfo reviewer) {
        ThemedBadge badge = new ThemedBadge(
            reviewer.getName() + "  ·  " + reviewer.getStatus().getDisplayName());
        badge.setCustomColor(reviewer.getStatus().getColor());
        badge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        badge.setToolTipText("Click to change status");

        badge.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ThemedPopupMenu menu = new ThemedPopupMenu();
                for (ReviewerStatus status : ReviewerStatus.values()) {
                    ThemedMenuItem item = new ThemedMenuItem(status.getDisplayName());
                    item.addActionListener(ignored -> {
                        reviewer.setStatus(status);
                        reviewContextManager.setReviewContext(reviewContextManager.getReviewContext());
                    });
                    menu.add(item);
                }
                menu.show(badge, e.getX(), e.getY());
            }
        });
        return badge;
    }

    private void updateStatusBadge(ReviewContext context) {
        if (context != null) {
            headerStatusBadge.setText(context.status.toString());
            headerStatusBadge.setCustomColor(context.status.getColor());
        } else {
            headerStatusBadge.setText("Unknown");
            headerStatusBadge.setCustomColor(Color.DARK_GRAY);
        }
    }

    private void updateButtonStates() {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context != null) {
            boolean allApproved = !context.reviewers.isEmpty()
                && context.reviewers.stream().allMatch(r -> r.getStatus() == ReviewerStatus.APPROVED);
            closeReviewButton.setEnabled(allApproved);
            closeReviewButton.setToolTipText(allApproved
                ? "All reviewers have approved – close the review"
                : "All reviewers must approve before closing");
        } else {
            closeReviewButton.setEnabled(false);
            closeReviewButton.setToolTipText("No review context available");
        }
    }


    private void setupListeners() {
        editReviewButton.addActionListener(ignored -> handleEditReview());
        closeReviewButton.addActionListener(ignored -> handleCloseReview());
    }

    private void handleEditReview() {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null) return;

        EditReviewDialog dialog = new EditReviewDialog(
            SwingUtilities.getWindowAncestor(this),
            context,
            reviewFormModels
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            ReviewContext updatedContext = dialog.getUpdatedContext();
            reviewContextManager.setReviewContext(updatedContext);
        }
    }

    private void handleCloseReview() {
        ReviewContext context = reviewContextManager.getReviewContext();
        if (context == null) return;

        if (context.status == ReviewStatus.COMPLETED) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Review is already completed.",
                "Close Review",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        context.status = ReviewStatus.COMPLETED;
        reviewContextManager.setReviewContext(context);

        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(this),
            "Review has been marked as completed.",
            "Close Review",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void setLabelText(ThemedLabel label, Supplier<String> textSetter) {
        SwingUtilities.invokeLater(() -> {
            if (reviewContextManager.getReviewContext() != null) {
                label.setText(textSetter.get());
            } else {
                label.setText("");
            }
        });
    }
}
