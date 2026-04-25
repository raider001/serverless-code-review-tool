package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.ScalableComponent;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTextField;
import com.kalynx.serverlessreviewtool.theme.components.ThemedComboBox;
import com.kalynx.serverlessreviewtool.theme.components.ThemedList;
import com.kalynx.serverlessreviewtool.theme.components.ThemedScrollPane;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTitledBorder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * ReviewSelectionPanel - Main UI for selecting and filtering code reviews
 */
public class ReviewSelectionPanel extends ThemedPanel {

    private final ThemeManager themeManager;
    private ThemedTextField titleFilter;
    private ThemedTextField authorFilter;
    private ThemedComboBox<String> repositoryFilter;
    private ThemedList<ReviewItem> reviewList;
    private DefaultListModel<ReviewItem> reviewListModel;
    private ThemedScrollPane scrollPane;
    private ThemedPanel filterPanel;
    private ThemedPanel listPanel;

    public ReviewSelectionPanel() {
        this.themeManager = ThemeManager.getInstance();
        setLayout(new BorderLayout(themeManager.scale(10), themeManager.scale(10)));
        setBackground(themeManager.getCurrentTheme().getBackgroundColor());
        setOpaque(true);

        // Create the filter panel at the top
        add(createFilterPanel(), BorderLayout.NORTH);

        // Create the scrollable review list in the center
        add(createReviewListPanel(), BorderLayout.CENTER);

        // Add some sample data
        loadSampleReviews();
    }

    /**
     * Create the filter panel with search/filter controls
     */
    private ThemedPanel createFilterPanel() {
        filterPanel = new ThemedPanel();
        // Use BoxLayout to keep components in a single horizontal row (no wrapping)
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            ThemedTitledBorder.create("Filters"),
            BorderFactory.createEmptyBorder(
                themeManager.scale(5),
                themeManager.scale(8),
                themeManager.scale(5),
                themeManager.scale(8)
            )
        ));

        // Create document listener for instant filtering
        DocumentListener instantFilter = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        };

        // Title filter
        ThemedLabel titleLabel = new ThemedLabel("Title:");
        titleLabel.setMaximumSize(new Dimension(themeManager.scale(50), themeManager.scale(30)));
        filterPanel.add(titleLabel);
        filterPanel.add(Box.createHorizontalStrut(themeManager.scale(5)));

        titleFilter = new ThemedTextField(15);
        titleFilter.setToolTipText("Filter by title");
        titleFilter.setMaximumSize(new Dimension(themeManager.scale(150), themeManager.scale(30)));
        titleFilter.setPreferredSize(new Dimension(themeManager.scale(150), themeManager.scale(25)));
        titleFilter.getDocument().addDocumentListener(instantFilter);
        filterPanel.add(titleFilter);
        filterPanel.add(Box.createHorizontalStrut(themeManager.scale(10)));

        // Author filter
        ThemedLabel authorLabel = new ThemedLabel("Author:");
        authorLabel.setMaximumSize(new Dimension(themeManager.scale(60), themeManager.scale(30)));
        filterPanel.add(authorLabel);
        filterPanel.add(Box.createHorizontalStrut(themeManager.scale(5)));

        authorFilter = new ThemedTextField(12);
        authorFilter.setToolTipText("Filter by author");
        authorFilter.setMaximumSize(new Dimension(themeManager.scale(120), themeManager.scale(30)));
        authorFilter.setPreferredSize(new Dimension(themeManager.scale(120), themeManager.scale(25)));
        authorFilter.getDocument().addDocumentListener(instantFilter);
        filterPanel.add(authorFilter);
        filterPanel.add(Box.createHorizontalStrut(themeManager.scale(10)));

        // Repository filter
        ThemedLabel repoLabel = new ThemedLabel("Repository:");
        repoLabel.setMaximumSize(new Dimension(themeManager.scale(80), themeManager.scale(30)));
        filterPanel.add(repoLabel);
        filterPanel.add(Box.createHorizontalStrut(themeManager.scale(5)));

        String[] repositories = {"All Repositories", "frontend-app", "backend-api", "mobile-app", "shared-lib"};
        repositoryFilter = new ThemedComboBox<>(repositories);
        repositoryFilter.setMaximumSize(new Dimension(themeManager.scale(150), themeManager.scale(30)));
        repositoryFilter.setPreferredSize(new Dimension(themeManager.scale(150), themeManager.scale(25)));
        repositoryFilter.addActionListener(e -> applyFilters());
        filterPanel.add(repositoryFilter);

        // Add glue at the end to push everything to the left
        filterPanel.add(Box.createHorizontalGlue());

        return filterPanel;
    }

    /**
     * Create the scrollable review list panel
     */
    private ThemedPanel createReviewListPanel() {
        listPanel = new ThemedPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(ThemedTitledBorder.create("Reviews"));

        // Create the list model and list
        reviewListModel = new DefaultListModel<>();
        reviewList = new ThemedList<>(reviewListModel);
        reviewList.setCellRenderer(new ReviewItemRenderer());

        // Add selection listener
        reviewList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ReviewItem selected = reviewList.getSelectedValue();
                if (selected != null) {
                    onReviewSelected(selected);
                }
            }
        });

        // Wrap in scroll pane
        scrollPane = new ThemedScrollPane(reviewList);

        listPanel.add(scrollPane, BorderLayout.CENTER);

        return listPanel;
    }

    /**
     * Apply filters to the review list
     */
    private void applyFilters() {
        String titleText = titleFilter.getText().toLowerCase().trim();
        String authorText = authorFilter.getText().toLowerCase().trim();
        String repoSelection = (String) repositoryFilter.getSelectedItem();

        // This is a placeholder - in a real app, you'd query your data source
        System.out.println("Applying filters:");
        System.out.println("  Title: " + (titleText.isEmpty() ? "<all>" : titleText));
        System.out.println("  Author: " + (authorText.isEmpty() ? "<all>" : authorText));
        System.out.println("  Repository: " + repoSelection);

        // TODO: Implement actual filtering logic here
    }

    /**
     * Handle review selection
     */
    private void onReviewSelected(ReviewItem review) {
        System.out.println("Selected review: " + review.getTitle());
        // TODO: Navigate to review details view
    }

    /**
     * Load sample reviews for demonstration
     */
    private void loadSampleReviews() {
        reviewListModel.addElement(new ReviewItem(
            "Add OAuth2 authentication flow",
            "John Doe",
            "backend-api",
            "Approved",
            "2 days ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Implement responsive navigation menu",
            "Jane Smith",
            "frontend-app",
            "Pending",
            "5 hours ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Fix memory leak in image processing",
            "Bob Johnson",
            "mobile-app",
            "Changes Requested",
            "1 day ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Update database migration scripts",
            "Alice Williams",
            "backend-api",
            "Approved",
            "3 days ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Add unit tests for user service",
            "Charlie Brown",
            "backend-api",
            "Pending",
            "6 hours ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Refactor authentication middleware",
            "Diana Prince",
            "shared-lib",
            "Approved",
            "1 week ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Implement dark mode support",
            "Eve Anderson",
            "frontend-app",
            "Pending",
            "2 hours ago"
        ));

        reviewListModel.addElement(new ReviewItem(
            "Optimize database queries",
            "Frank Miller",
            "backend-api",
            "Changes Requested",
            "4 days ago"
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme and recreate titled borders if needed
        // TitledBorders are immutable and must be recreated to pick up new theme colors
        Theme theme = themeManager.getCurrentTheme();

        // Recreate filter panel border with current theme colors
        if (filterPanel != null) {
            javax.swing.border.Border newBorder = BorderFactory.createCompoundBorder(
                ThemedTitledBorder.create("Filters"),
                BorderFactory.createEmptyBorder(
                    themeManager.scale(5),
                    themeManager.scale(8),
                    themeManager.scale(5),
                    themeManager.scale(8)
                )
            );
            filterPanel.setBorder(newBorder);
            filterPanel.setForeground(theme.getForegroundColor());
        }

        // Recreate list panel border with current theme colors
        if (listPanel != null) {
            javax.swing.border.Border newBorder = ThemedTitledBorder.create("Reviews");
            listPanel.setBorder(newBorder);
            listPanel.setForeground(theme.getForegroundColor());
        }

        super.paintComponent(g);
    }

    /**
     * ReviewItem - Data model for a review
     */
    public static class ReviewItem {
        private final String title;
        private final String author;
        private final String repository;
        private final String status;
        private final String timeAgo;

        public ReviewItem(String title, String author, String repository, String status, String timeAgo) {
            this.title = title;
            this.author = author;
            this.repository = repository;
            this.status = status;
            this.timeAgo = timeAgo;
        }

        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getRepository() { return repository; }
        public String getStatus() { return status; }
        public String getTimeAgo() { return timeAgo; }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * ReviewItemRenderer - Custom renderer for review list items
     */
    private class ReviewItemRenderer extends ThemedPanel implements ListCellRenderer<ReviewItem> {
        private final JLabel titleLabel;
        private final JLabel metadataLabel;
        private final JLabel statusLabel;
        private final JLabel timeLabel;

        public ReviewItemRenderer() {
            setLayout(new BorderLayout(themeManager.scale(10), themeManager.scale(5)));
            setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(8),
                themeManager.scale(12),
                themeManager.scale(8),
                themeManager.scale(12)
            ));

            // Left side: Title and metadata
            ThemedPanel leftPanel = new ThemedPanel();
            leftPanel.setOpaque(false);
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

            titleLabel = new JLabel();
            titleLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.BOLD, 13));

            metadataLabel = new JLabel();
            metadataLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.PLAIN, 11));

            leftPanel.add(titleLabel);
            leftPanel.add(Box.createVerticalStrut(themeManager.scale(4)));
            leftPanel.add(metadataLabel);

            // Right side: Status and time
            ThemedPanel rightPanel = new ThemedPanel();
            rightPanel.setOpaque(false);
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            statusLabel = new JLabel();
            statusLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.BOLD, 11));
            statusLabel.setOpaque(true);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(2),
                themeManager.scale(6),
                themeManager.scale(2),
                themeManager.scale(6)
            ));

            timeLabel = new JLabel();
            timeLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.PLAIN, 10));

            rightPanel.add(statusLabel);
            rightPanel.add(Box.createVerticalStrut(themeManager.scale(4)));
            rightPanel.add(timeLabel);

            add(leftPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ReviewItem> list, ReviewItem value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Theme theme = themeManager.getCurrentTheme();

            // Set background based on selection
            if (isSelected) {
                setBackground(theme.getAccentColor());
                titleLabel.setForeground(Color.WHITE);
                metadataLabel.setForeground(new Color(230, 230, 230));
                timeLabel.setForeground(new Color(230, 230, 230));
            } else {
                setBackground(index % 2 == 0 ? theme.getBackgroundColor() : theme.getButtonBackground());
                titleLabel.setForeground(theme.getForegroundColor());
                metadataLabel.setForeground(theme.getForegroundColor());
                timeLabel.setForeground(theme.getForegroundColor());
            }

            // Set content
            titleLabel.setText(value.getTitle());
            metadataLabel.setText(value.getAuthor() + " • " + value.getRepository());
            timeLabel.setText(value.getTimeAgo());

            // Set status badge color
            switch (value.getStatus().toLowerCase()) {
                case "approved":
                    statusLabel.setBackground(theme.getApprovedColor());
                    statusLabel.setForeground(Color.WHITE);
                    break;
                case "pending":
                    statusLabel.setBackground(theme.getPendingColor());
                    statusLabel.setForeground(Color.BLACK);
                    break;
                case "changes requested":
                    statusLabel.setBackground(theme.getChangesRequestedColor());
                    statusLabel.setForeground(Color.WHITE);
                    break;
                default:
                    statusLabel.setBackground(theme.getButtonBackground());
                    statusLabel.setForeground(theme.getForegroundColor());
            }
            statusLabel.setText(value.getStatus());

            return this;
        }
    }
}























