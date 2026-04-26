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
import com.kalynx.serverlessreviewtool.theme.components.ThemedButton;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTabbedPane;

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

    // Three separate lists for each tab
    private ThemedList<ReviewItem> myReviewsList;
    private DefaultListModel<ReviewItem> myReviewsListModel;

    private ThemedList<ReviewItem> toReviewList;
    private DefaultListModel<ReviewItem> toReviewListModel;

    private ThemedList<ReviewItem> completedList;
    private DefaultListModel<ReviewItem> completedListModel;

    private ThemedScrollPane scrollPane;
    private ThemedPanel filterPanel;
    private ThemedPanel listPanel;
    private ThemedTabbedPane tabbedPane;

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
     * Create the scrollable review list panel with tabs
     */
    private ThemedPanel createReviewListPanel() {
        listPanel = new ThemedPanel();
        listPanel.setLayout(new BorderLayout());

        // Create tabbed pane
        tabbedPane = new ThemedTabbedPane();

        // Tab 1: My Reviews - Reviews created by the user
        myReviewsListModel = new DefaultListModel<>();
        myReviewsList = new ThemedList<>(myReviewsListModel);
        myReviewsList.setCellRenderer(new ReviewItemRenderer());
        myReviewsList.onItemSelected(this::onReviewSelected);
        ThemedScrollPane myReviewsScrollPane = new ThemedScrollPane(myReviewsList);
        tabbedPane.addTab("My Reviews", myReviewsScrollPane);

        // Tab 2: To Review - Reviews waiting for the user's response
        toReviewListModel = new DefaultListModel<>();
        toReviewList = new ThemedList<>(toReviewListModel);
        toReviewList.setCellRenderer(new ReviewItemRenderer());
        toReviewList.onItemSelected(this::onReviewSelected);
        ThemedScrollPane toReviewScrollPane = new ThemedScrollPane(toReviewList);
        tabbedPane.addTab("To Review", toReviewScrollPane);

        // Tab 3: Completed - Reviews user has responded to, waiting for acknowledgement
        completedListModel = new DefaultListModel<>();
        completedList = new ThemedList<>(completedListModel);
        completedList.setCellRenderer(new ReviewItemRenderer());
        completedList.onItemSelected(this::onReviewSelected);
        ThemedScrollPane completedScrollPane = new ThemedScrollPane(completedList);
        tabbedPane.addTab("Completed", completedScrollPane);

        listPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel with Create Review button
        ThemedPanel bottomPanel = new ThemedPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, themeManager.scale(10), themeManager.scale(5)));
        bottomPanel.setOpaque(false);

        ThemedButton createReviewButton = new ThemedButton("Create Review");
        createReviewButton.setPreferredSize(new Dimension(themeManager.scale(130), themeManager.scale(32)));
        createReviewButton.setToolTipText("Create a new code review");

        // Use accent style to make it prominent
        createReviewButton.setAccentStyle(true);

        createReviewButton.addActionListener(e -> onCreateReview());
        bottomPanel.add(createReviewButton);

        listPanel.add(bottomPanel, BorderLayout.SOUTH);

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
     * Handle create review button click
     */
    private void onCreateReview() {
        // Get list of available repositories (in real app, fetch from data source)
        java.util.List<String> availableRepositories = java.util.Arrays.asList(
            "frontend-app", "backend-api", "mobile-app", "shared-lib"
        );

        // Get list of available reviewers (in real app, fetch from data source)
        java.util.List<String> availableReviewers = java.util.Arrays.asList(
            "John Doe", "Jane Smith", "Bob Johnson", "Eve Anderson", "Michael Scott", "Sarah Connor", "James Bond", "Tony Stark"
        );

        CreateReviewDialog dialog = new CreateReviewDialog(SwingUtilities.getWindowAncestor(this), availableRepositories, availableReviewers);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String title = dialog.getReviewTitle();
            String summary = dialog.getSummary();
            boolean isBranchMode = dialog.isBranchMode();
            java.util.List<String> selectedRepos = dialog.getSelectedRepositories();
            java.util.List<String> selectedReviewers = dialog.getSelectedReviewers();

            System.out.println("Creating review:");
            System.out.println("  Title: " + title);
            System.out.println("  Summary: " + summary);
            System.out.println("  Mode: " + (isBranchMode ? "Branch-based" : "Commit-based"));
            System.out.println("  Repositories: " + selectedRepos);
            System.out.println("  Reviewers: " + selectedReviewers);

            // TODO: Create the review in the backend and navigate to review details
        }
    }

    /**
     * Load sample reviews for demonstration
     */
    private void loadSampleReviews() {
        loadMyReviews();
        loadToReviewReviews();
        loadCompletedReviews();

        // Update tab counts
        updateTabCounts();
    }

    /**
     * Update the counts displayed in each tab
     */
    private void updateTabCounts() {
        tabbedPane.setTabTitleWithCount(0, "My Reviews", myReviewsListModel.getSize());
        tabbedPane.setTabTitleWithCount(1, "To Review", toReviewListModel.getSize());
        tabbedPane.setTabTitleWithCount(2, "Completed", completedListModel.getSize());
    }

    /**
     * Load "My Reviews" - Reviews created by the user
     */
    private void loadMyReviews() {
        myReviewsListModel.clear();

        myReviewsListModel.addElement(new ReviewItem(
            "Update database migration scripts",
            "You",
            "backend-api",
            "Pending",
            "3 days ago"
        ));

        myReviewsListModel.addElement(new ReviewItem(
            "Add unit tests for user service",
            "You",
            "backend-api",
            "Approved",
            "6 hours ago"
        ));

        myReviewsListModel.addElement(new ReviewItem(
            "Refactor authentication middleware",
            "You",
            "shared-lib",
            "Changes Requested",
            "1 week ago"
        ));

        myReviewsListModel.addElement(new ReviewItem(
            "Optimize database queries",
            "You",
            "backend-api",
            "Pending",
            "4 days ago"
        ));
    }

    /**
     * Load "To Review" - Reviews waiting for the user's response
     */
    private void loadToReviewReviews() {
        toReviewListModel.clear();

        toReviewListModel.addElement(new ReviewItem(
            "Add OAuth2 authentication flow",
            "John Doe",
            "backend-api",
            "Pending",
            "2 days ago"
        ));

        toReviewListModel.addElement(new ReviewItem(
            "Implement responsive navigation menu",
            "Jane Smith",
            "frontend-app",
            "Pending",
            "5 hours ago"
        ));

        toReviewListModel.addElement(new ReviewItem(
            "Fix memory leak in image processing",
            "Bob Johnson",
            "mobile-app",
            "Pending",
            "1 day ago"
        ));

        toReviewListModel.addElement(new ReviewItem(
            "Implement dark mode support",
            "Eve Anderson",
            "frontend-app",
            "Pending",
            "2 hours ago"
        ));

        toReviewListModel.addElement(new ReviewItem(
            "Add GraphQL API endpoints",
            "Michael Scott",
            "backend-api",
            "Pending",
            "12 hours ago"
        ));
    }

    /**
     * Load "Completed" - Reviews user has responded to, waiting for acknowledgement
     */
    private void loadCompletedReviews() {
        completedListModel.clear();

        completedListModel.addElement(new ReviewItem(
            "Implement user profile editing",
            "Sarah Connor",
            "frontend-app",
            "Approved",
            "1 day ago"
        ));

        completedListModel.addElement(new ReviewItem(
            "Fix security vulnerability in auth",
            "James Bond",
            "backend-api",
            "Approved",
            "3 hours ago"
        ));

        completedListModel.addElement(new ReviewItem(
            "Add Redis caching layer",
            "Tony Stark",
            "backend-api",
            "Changes Requested",
            "2 days ago"
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

        // Recreate list panel styling with current theme colors
        if (listPanel != null) {
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
        private final ThemedLabel titleLabel;
        private final ThemedLabel metadataLabel;
        private final ThemedLabel statusLabel;
        private final ThemedLabel timeLabel;

        public ReviewItemRenderer() {
            setLayout(new BorderLayout(themeManager.scale(10), themeManager.scale(2)));
            setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(4),
                themeManager.scale(12),
                themeManager.scale(4),
                themeManager.scale(12)
            ));

            // Left side: Title and metadata
            ThemedPanel leftPanel = new ThemedPanel();
            leftPanel.setOpaque(false);
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

            titleLabel = new ThemedLabel();
            titleLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.BOLD, 13));

            metadataLabel = new ThemedLabel();
            metadataLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.PLAIN, 11));

            leftPanel.add(titleLabel);
            leftPanel.add(Box.createVerticalStrut(themeManager.scale(2)));
            leftPanel.add(metadataLabel);

            // Right side: Status and time
            ThemedPanel rightPanel = new ThemedPanel();
            rightPanel.setOpaque(false);
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            statusLabel = new ThemedLabel();
            statusLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.BOLD, 11));
            statusLabel.setOpaque(true);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(
                themeManager.scale(2),
                themeManager.scale(6),
                themeManager.scale(2),
                themeManager.scale(6)
            ));

            timeLabel = new ThemedLabel();
            timeLabel.setFont(ScalableComponent.createScaledFont("Segoe UI", Font.PLAIN, 10));

            rightPanel.add(statusLabel);
            rightPanel.add(Box.createVerticalStrut(themeManager.scale(2)));
            rightPanel.add(timeLabel);

            add(leftPanel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Override to prevent ThemedPanel from resetting background
            // Just paint the background color that was set in getListCellRendererComponent
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
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























