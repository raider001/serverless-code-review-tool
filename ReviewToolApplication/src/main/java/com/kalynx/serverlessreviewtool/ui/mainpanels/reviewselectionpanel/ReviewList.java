package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.models.ReviewItem;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedList;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel.reviewitem.ReviewItemCellRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class ReviewList extends ThemedList<ReviewItem> {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient Consumer<ReviewItem> doubleClickCallback;

    public ReviewList() {
        super(new ReviewItemFilterableModel());
        setCellRenderer(new ReviewItemCellRenderer());
        onItemSelected(this::onReviewSelected);
        setupDoubleClickListener();
    }

    private void setupDoubleClickListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && doubleClickCallback != null) {
                    int index = locationToIndex(e.getPoint());
                    if (index >= 0) {
                        ReviewItem item = getModel().getElementAt(index);
                        if (item != null) {
                            doubleClickCallback.accept(item);
                        }
                    }
                }
            }
        });
    }

    public void onDoubleClick(Consumer<ReviewItem> callback) {
        this.doubleClickCallback = callback;
    }

    private void onReviewSelected(ReviewItem reviewItem) {
        System.out.println("Selected review: " + reviewItem.getTitle());
    }

    public void setFilters(String titleFilter, String authorFilter, List<String> repositoryFilter) {
        ((ReviewItemFilterableModel) getModel()).filter(titleFilter, authorFilter, repositoryFilter);
    }

    private static class ReviewItemFilterableModel extends FilterableDefaultListModel<ReviewItem> {
        @Serial
        private static final long serialVersionUID = 1L;

        public ReviewItemFilterableModel() {
            super(ReviewItemFilterableModel::matchesFilters);
        }

        private static boolean matchesFilters(ReviewItem item, String titleFilter,
                                             String authorFilter, List<String> repositoryFilter) {
            if (!titleFilter.isEmpty()) {
                if (!item.getTitle().toLowerCase().contains(titleFilter)) {
                    return false;
                }
            }

            if (!authorFilter.isEmpty()) {
                if (!item.getAuthor().toLowerCase().contains(authorFilter)) {
                    return false;
                }
            }

            if (repositoryFilter != null && !repositoryFilter.isEmpty()) {
                return item.getRepositories().stream()
                    .anyMatch(repositoryFilter::contains);
            }

            return true;
        }
    }
}
