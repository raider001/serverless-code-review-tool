package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel;

import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.models.ReviewStatus;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedScrollPane;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTabbedPane;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ReviewListPanel extends ThemedPanel {

    private final ReviewItemManager reviewItemManager = ReviewItemManager.getInstance();

    private final ThemedTabbedPane tabbedPane = new ThemedTabbedPane();
    private final ReviewList       myReviewsList  = new ReviewList();
    private final ReviewList       myOpenReviewsList = new ReviewList();
    private final ReviewList       completedReviewsList = new ReviewList();

    public ReviewListPanel() {
         configureLayout();
         updateTabs();
         setupListeners();
    }

    private void configureLayout() {
         setLayout(new MigLayout("", "[grow]", "[grow]"));

         ThemedScrollPane myReviewsScrollPane = new ThemedScrollPane(myReviewsList);
         tabbedPane.addTab("My Reviews", myReviewsScrollPane);
         ThemedScrollPane myOpenReviewsScrollPane = new ThemedScrollPane(myOpenReviewsList);
         tabbedPane.addTab("Open Reviews", myOpenReviewsScrollPane);

         ThemedScrollPane completedReviewsScrollPane = new ThemedScrollPane(completedReviewsList);
         tabbedPane.addTab("Completed Reviews", completedReviewsScrollPane);

         add(tabbedPane, "cell 0 0, grow");
     }

    private void updateTabs() {
         tabbedPane.setTabTitleWithCount(0, "My Reviews", myReviewsList.getModel().getSize());
         tabbedPane.setTabTitleWithCount(1, "Open Reviews", myOpenReviewsList.getModel().getSize());
         tabbedPane.setTabTitleWithCount(2, "Completed", completedReviewsList.getModel().getSize());
     }

    private void setupListeners() {
        reviewItemManager.addListener(this::loadSampleData);
    }

    public void filterLists(String titleFilter, String authorFilter, List<String> repositoryFilter) {
        myReviewsList.setFilters(titleFilter, authorFilter, repositoryFilter);
        myOpenReviewsList.setFilters(titleFilter, authorFilter, repositoryFilter);
        completedReviewsList.setFilters(titleFilter, authorFilter, repositoryFilter);
        updateTabs();
    }

    private void updateModel(DefaultListModel<ReviewItem> model, List<ReviewItem> reviews) {
        SwingUtilities.invokeLater(() -> {
            model.clear();
            reviews.forEach(model::addElement);
            updateTabs();
        });
    }

    private void loadSampleData(List<ReviewItem> reviews) {
        // Filter and update each list asynchronously
        // TODO - Hook this into git config for current user identification

        CompletableFuture.runAsync(() -> {
            List<ReviewItem> myReviews = reviews.stream()
                .filter(r -> r.getAuthor().equals("You") && r.getStatus() != ReviewStatus.COMPLETED)
                .collect(Collectors.toList());
            updateModel((DefaultListModel<ReviewItem>) myReviewsList.getModel(), myReviews);
        });

        CompletableFuture.runAsync(() -> {
            List<ReviewItem> openReviews = reviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.OPEN && !r.getAuthor().equals("You"))
                .collect(Collectors.toList());
            updateModel((DefaultListModel<ReviewItem>) myOpenReviewsList.getModel(), openReviews);
        });

        CompletableFuture.runAsync(() -> {
            List<ReviewItem> completedReviews = reviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.COMPLETED)
                .collect(Collectors.toList());
            updateModel((DefaultListModel<ReviewItem>) completedReviewsList.getModel(), completedReviews);
        });
    }
}
