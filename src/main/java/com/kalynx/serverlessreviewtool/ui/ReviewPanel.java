package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.review.*;
import com.kalynx.serverlessreviewtool.ui.review.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * ReviewPanel - Main panel for reviewing code changes across multiple repositories
 * Supports file navigation, diff viewing (side-by-side and unified), commit comparison, and inline comments
 */
public class ReviewPanel extends ThemedPanel {

    private ReviewContext reviewContext;

    // Main components
    private ThemedPanel          northWrapper;
    private CommitSelectorPanel  commitSelectorPanel;
    private FileNavigationPanel  fileNavigationPanel;
    private DiffViewerPanel      diffViewerPanel;

    // Mutable header sub-components – updated in-place, never removed/re-added
    private ThemedBadge  headerStatusBadge;
    private ThemedLabel  headerTitleLabel;
    private ThemedLabel  headerAuthorLabel;
    private ThemedLabel  headerSummaryLabel;
    private ThemedPanel  headerReviewerRow;
    private ThemedButton headerCloseReviewBtn;

    // Footer action buttons
    private ThemedButton requestChangesBtn;
    private ThemedButton completeReviewBtn;

    public ReviewPanel() {
        setLayout(new BorderLayout());

        // Initialize with sample review context
        initializeSampleReviewContext();

        // Build UI
        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Commit selector at the top
        commitSelectorPanel = new CommitSelectorPanel(reviewContext);
        commitSelectorPanel.addCommitSelectionListener(this::onCommitSelectionChanged);
        commitSelectorPanel.addViewModeListener(this::onViewModeChanged);

        // File navigation on the left
        fileNavigationPanel = new FileNavigationPanel(reviewContext);
        fileNavigationPanel.addFileSelectionListener(this::onFileSelected);

        // Diff viewer in the center
        diffViewerPanel = new DiffViewerPanel();
    }

    private void layoutComponents() {
        northWrapper = new ThemedPanel(new BorderLayout());
        northWrapper.add(buildReviewHeader(reviewContext), BorderLayout.NORTH);
        northWrapper.add(commitSelectorPanel,              BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

        ThemedSplitPane mainSplitPane = new ThemedSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(fileNavigationPanel);
        mainSplitPane.setRightComponent(diffViewerPanel);
        mainSplitPane.setResizeWeight(0.20);
        add(mainSplitPane, BorderLayout.CENTER);

        add(buildActionFooter(), BorderLayout.SOUTH);
    }

    /**
     * Updates all mutable header components in-place — no panel is ever removed or re-added.
     * Calling revalidate/repaint on the northWrapper is enough to adjust sizing.
     */
    private void refreshHeader() {
        ReviewContext ctx = reviewContext;

        // Status badge
        headerStatusBadge.setCustomColor(ctx.getReviewStatus().getColor());
        // ThemedBadge text is final — recreate only the badge inside titleGroup
        // (the titleGroup FlowLayout panel itself stays put)
        Component titleGroup = headerStatusBadge.getParent();
        if (titleGroup instanceof ThemedPanel) {
            ((ThemedPanel) titleGroup).remove(headerStatusBadge);
            headerStatusBadge = new ThemedBadge(ctx.getReviewStatus().getDisplayName(), null);
            headerStatusBadge.setCustomColor(ctx.getReviewStatus().getColor());
            ((ThemedPanel) titleGroup).add(headerStatusBadge, 0); // leftmost
            titleGroup.revalidate();
            titleGroup.repaint();
        }

        // Title
        headerTitleLabel.setText(ctx.getTitle());

        // Author
        headerAuthorLabel.setText("by " + ctx.getAuthor());
        headerAuthorLabel.setVisible(!ctx.getAuthor().isBlank());

        // Summary
        headerSummaryLabel.setText("<html>" + ctx.getSummary() + "</html>");
        headerSummaryLabel.setVisible(!ctx.getSummary().isBlank());

        // Reviewer badges — only the FlowLayout row content changes
        headerReviewerRow.removeAll();
        for (ReviewerInfo info : ctx.getReviewers()) {
            headerReviewerRow.add(createStatusBadge(info));
        }
        headerReviewerRow.setVisible(!ctx.getReviewers().isEmpty());
        headerReviewerRow.revalidate();
        headerReviewerRow.repaint();

        // Close Review button
        boolean allApproved = !ctx.getReviewers().isEmpty()
            && ctx.getReviewers().stream().allMatch(r -> r.getStatus() == ReviewerStatus.APPROVED);
        headerCloseReviewBtn.setEnabled(allApproved);
        headerCloseReviewBtn.setToolTipText(allApproved
            ? "All reviewers have approved – close the review"
            : "All reviewers must approve before closing");

        // Footer buttons
        refreshActionButtons();

        northWrapper.revalidate();
        northWrapper.repaint();
    }

    /** Update enabled state of the footer action buttons. */
    private void refreshActionButtons() {
        if (requestChangesBtn != null)
            requestChangesBtn.setEnabled(reviewContext.getReviewStatus() != ReviewStatus.CHANGES_REQUESTED);
        if (completeReviewBtn != null)
            completeReviewBtn.setEnabled(reviewContext.getReviewStatus() != ReviewStatus.COMPLETED);
    }

    /** Bottom-right panel: Request Changes + Complete Review. */
    private ThemedPanel buildActionFooter() {
        ThemedPanel footer = new ThemedPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, ThemeManager.getInstance().getCurrentTheme().getBorderColor()));

        requestChangesBtn = new ThemedButton("Request Changes");
        requestChangesBtn.setEnabled(reviewContext.getReviewStatus() != ReviewStatus.CHANGES_REQUESTED);
        requestChangesBtn.addActionListener(e -> {
            reviewContext.setReviewStatus(ReviewStatus.CHANGES_REQUESTED);
            refreshHeader();
        });

        completeReviewBtn = new ThemedButton("Complete Review");
        completeReviewBtn.setAccentStyle(true);
        completeReviewBtn.setEnabled(reviewContext.getReviewStatus() != ReviewStatus.COMPLETED);
        completeReviewBtn.addActionListener(e -> {
            reviewContext.setReviewStatus(ReviewStatus.COMPLETED);
            refreshHeader();
        });

        footer.add(requestChangesBtn);
        footer.add(completeReviewBtn);
        return footer;
    }

    /**
     * Builds the review header once. All mutable sub-components are stored as
     * fields so they can be updated in-place by {@link #refreshHeader()}.
     * Uses MigLayout hidemode 3 so invisible rows collapse cleanly.
     */
    private ThemedPanel buildReviewHeader(ReviewContext ctx) {
        ThemeManager tm = ThemeManager.getInstance();

        ThemedPanel header = new ThemedPanel();
        header.setLayout(new net.miginfocom.swing.MigLayout(
            "insets 10 12 10 12, gap 4 0, hidemode 3",
            "[grow,fill][][]",
            "[]8[]6[]10[]"
        ));
        header.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, tm.getCurrentTheme().getBorderColor()));

        // ── Row 1: [badge · Title ····] [Edit] [Close Review] ─────────────
        headerStatusBadge = new ThemedBadge(ctx.getReviewStatus().getDisplayName(), null);
        headerStatusBadge.setCustomColor(ctx.getReviewStatus().getColor());

        headerTitleLabel = new ThemedLabel(ctx.getTitle()) {
            @Override protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getCurrentTheme().getForegroundColor());
                super.paintComponent(g);
            }
        };
        headerTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, tm.scale(15)));

        ThemedPanel titleGroup = new ThemedPanel(new FlowLayout(FlowLayout.LEFT, tm.scale(8), 0));
        titleGroup.setOpaque(false);
        titleGroup.add(headerStatusBadge);
        titleGroup.add(headerTitleLabel);

        boolean allApproved = !ctx.getReviewers().isEmpty()
            && ctx.getReviewers().stream().allMatch(r -> r.getStatus() == ReviewerStatus.APPROVED);
        headerCloseReviewBtn = new ThemedButton("Close Review");
        headerCloseReviewBtn.setEnabled(allApproved);
        headerCloseReviewBtn.setToolTipText(allApproved
            ? "All reviewers have approved – close the review"
            : "All reviewers must approve before closing");
        headerCloseReviewBtn.addActionListener(e -> {
            reviewContext.setReviewStatus(ReviewStatus.COMPLETED);
            refreshHeader();
        });

        ThemedButton editBtn = new ThemedButton("Edit");
        editBtn.addActionListener(e -> openEditDialog());

        header.add(titleGroup, "growx");
        header.add(editBtn);
        header.add(headerCloseReviewBtn, "wrap");

        // ── Row 2: Author (hidden when blank) ─────────────────────────────
        headerAuthorLabel = new ThemedLabel("by " + ctx.getAuthor()) {
            @Override protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getCurrentTheme().getSecondaryTextColor());
                super.paintComponent(g);
            }
        };
        headerAuthorLabel.setFont(new Font("Segoe UI", Font.ITALIC, tm.scale(11)));
        headerAuthorLabel.setVisible(!ctx.getAuthor().isBlank());
        header.add(headerAuthorLabel, "growx, span 3, wrap");

        // ── Row 3: Summary (hidden when blank) ────────────────────────────
        headerSummaryLabel = new ThemedLabel("<html>" + ctx.getSummary() + "</html>") {
            @Override protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getCurrentTheme().getSecondaryTextColor());
                super.paintComponent(g);
            }
        };
        headerSummaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, tm.scale(11)));
        headerSummaryLabel.setVisible(!ctx.getSummary().isBlank());
        header.add(headerSummaryLabel, "growx, span 3, wrap");

        // ── Row 4: Reviewer status badges ─────────────────────────────────
        headerReviewerRow = new ThemedPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerReviewerRow.setOpaque(false);
        for (ReviewerInfo info : ctx.getReviewers()) {
            headerReviewerRow.add(createStatusBadge(info));
        }
        headerReviewerRow.setVisible(!ctx.getReviewers().isEmpty());
        header.add(headerReviewerRow, "growx, span 3");

        return header;
    }

    /** Creates a reviewer badge that shows a status popup on click. */
    private ThemedBadge createStatusBadge(ReviewerInfo info) {
        ThemedBadge badge = new ThemedBadge(
            info.getName() + "  ·  " + info.getStatus().getDisplayName(), null);
        badge.setCustomColor(info.getStatus().getColor());
        badge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        badge.setToolTipText("Click to change status");

        badge.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                for (ReviewerStatus status : ReviewerStatus.values()) {
                    JMenuItem item = new JMenuItem(status.getDisplayName());
                    item.addActionListener(evt -> {
                        info.setStatus(status);
                        refreshHeader();
                    });
                    menu.add(item);
                }
                menu.show(badge, e.getX(), e.getY());
            }
        });
        return badge;
    }

    /** Opens the EditReviewDialog and applies changes if confirmed. */
    private void openEditDialog() {
        // Derive available lists from current context
        List<String> repos = reviewContext.getRepositories().stream()
            .map(r -> r.getName())
            .collect(java.util.stream.Collectors.toList());
        List<String> reviewerNames = reviewContext.getReviewers().stream()
            .map(ReviewerInfo::getName)
            .collect(java.util.stream.Collectors.toList());

        EditReviewDialog dlg = new EditReviewDialog(this, reviewContext, repos, reviewerNames);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            dlg.applyTo(reviewContext);
            refreshHeader();
        }
    }

    /**
     * Initialize sample review context for demonstration
     */
    private void initializeSampleReviewContext() {
        reviewContext = new ReviewContext("REVIEW-123", "Implement OAuth2 authentication");
        reviewContext.setAuthor("John Doe");
        reviewContext.setSummary(
            "Adds full OAuth2 support across the backend API and frontend app, " +
            "including token validation, refresh token handling, and a new login UI component.");
        reviewContext.addReviewer("Alice Chen");
        reviewContext.addReviewer("Bob Martin");
        reviewContext.addReviewer("Carlos Rivera");

        // Add sample repositories
        Repository backendRepo = new Repository("backend-api", "https://github.com/company/backend-api");
        Repository frontendRepo = new Repository("frontend-app", "https://github.com/company/frontend-app");

        // Add sample commits to backend repo
        backendRepo.addCommit(new Commit("abc123", "Initial OAuth2 setup", "John Doe", "2024-04-20"));
        backendRepo.addCommit(new Commit("def456", "Add token validation", "John Doe", "2024-04-21"));
        backendRepo.addCommit(new Commit("ghi789", "Implement refresh token", "John Doe", "2024-04-22"));

        // Add sample files to backend repo
        backendRepo.addFile(new ReviewFile("src/auth/OAuthService.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/auth/TokenValidator.java", "backend-api", FileChangeType.ADDED));
        backendRepo.addFile(new ReviewFile("src/config/SecurityConfig.java", "backend-api", FileChangeType.MODIFIED));
        backendRepo.addFile(new ReviewFile("src/models/User.java", "backend-api", FileChangeType.MODIFIED));

        // Add sample commits to frontend repo
        frontendRepo.addCommit(new Commit("jkl012", "Add OAuth login UI", "Jane Smith", "2024-04-21"));
        frontendRepo.addCommit(new Commit("mno345", "Handle OAuth callbacks", "Jane Smith", "2024-04-22"));

        // Add sample files to frontend repo
        frontendRepo.addFile(new ReviewFile("src/components/LoginForm.tsx", "frontend-app", FileChangeType.MODIFIED));
        frontendRepo.addFile(new ReviewFile("src/services/AuthService.ts", "frontend-app", FileChangeType.ADDED));
        frontendRepo.addFile(new ReviewFile("src/utils/TokenStorage.ts", "frontend-app", FileChangeType.ADDED));

        reviewContext.addRepository(backendRepo);
        reviewContext.addRepository(frontendRepo);
    }

    /**
     * Handle commit selection change
     */
    private void onCommitSelectionChanged(Commit startCommit, Commit endCommit) {
        System.out.println("Commit selection changed: " + startCommit.getHash() + " -> " + endCommit.getHash());
        // Update file navigation to show files changed between these commits
        fileNavigationPanel.setCommitRange(startCommit, endCommit);
        // Update diff viewer if a file is selected
        ReviewFile selectedFile = fileNavigationPanel.getSelectedFile();
        if (selectedFile != null) {
            diffViewerPanel.showDiff(selectedFile, startCommit, endCommit);
        }
    }

    /**
     * Handle file selection
     */
    private void onFileSelected(ReviewFile file) {
        System.out.println("File selected: " + file.getPath());
        // Get current commit range
        Commit startCommit = commitSelectorPanel.getStartCommit();
        Commit endCommit = commitSelectorPanel.getEndCommit();

        // Show diff for this file
        diffViewerPanel.showDiff(file, startCommit, endCommit);
    }

    /**
     * Handle view mode change
     */
    private void onViewModeChanged(DiffViewMode mode) {
        System.out.println("View mode changed to: " + mode);
        // Update diff viewer with new view mode
        diffViewerPanel.setViewMode(mode);
    }

    /**
     * Set the review context
     */
    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        commitSelectorPanel.setReviewContext(context);
        fileNavigationPanel.setReviewContext(context);
        diffViewerPanel.clear();
        refreshHeader();
    }

    /**
     * Get the current review context
     */
    public ReviewContext getReviewContext() {
        return reviewContext;
    }

    /**
     * Refresh the entire review display — called by the title bar refresh button.
     */
    public void refresh() {
        refreshHeader();
    }
}

