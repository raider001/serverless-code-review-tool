package com.kalynx.serverlessreviewtool.ui.review;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedComboBox;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedLabel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedWindow;
import com.kalynx.serverlessreviewtool.theme.Theme;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * CommitSelectorPanel - Custom dual-slider for commit range selection
 * Features a horizontal slider with two thumbs to select start and end commits,
 * with commit hashes displayed above as notches
 */
public class CommitSelectorPanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ReviewContextManager reviewContextManager;

    private ThemedComboBox<DiffViewMode> viewModeComboBox;
    private CommitSliderPanel commitSliderPanel;

    private transient final List<CommitRangeListener> commitRangeListeners = new ArrayList<>();
    private transient final List<ViewModeListener> viewModeListeners = new ArrayList<>();

    public CommitSelectorPanel(ReviewContextManager reviewContextManager) {
        this.reviewContextManager = reviewContextManager;
        configureLayout();
        setupListeners();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fillx, insets 0", "[grow]10[][]", "[]"));

        commitSliderPanel = new CommitSliderPanel();
        add(commitSliderPanel, "growx, h 50!");

        ThemedLabel viewLabel = new ThemedLabel("View:");
        viewModeComboBox = new ThemedComboBox<>(DiffViewMode.values());
        viewModeComboBox.setSelectedItem(DiffViewMode.SIDE_BY_SIDE);
        viewModeComboBox.addActionListener(ignored -> onViewModeChanged());

        add(viewLabel);
        add(viewModeComboBox, "w 120!");
    }

    private void setupListeners() {
        reviewContextManager.addListener(this::onReviewContextChanged);
    }

    private void onReviewContextChanged(ReviewContext context) {
        if (context != null && !context.getRepositories().isEmpty()) {
            Repository firstRepo = context.getRepositories().getFirst();
            loadCommitsForRepository(firstRepo);
        }
    }

    private void onViewModeChanged() {
        DiffViewMode mode = (DiffViewMode) viewModeComboBox.getSelectedItem();
        if (mode != null) {
            fireViewModeChanged(mode);
        }
    }

    public void loadCommitsForRepository(Repository repository) {
        if (repository == null) return;

        List<Commit> commits = repository.getCommits();
        commitSliderPanel.setCommits(commits);

        if (!commits.isEmpty()) {
            commitSliderPanel.setStartIndex(0);
            commitSliderPanel.setEndIndex(commits.size() - 1);
            fireCommitRangeChanged(commits.getFirst(), commits.getLast());
        }
    }

    public Commit getStartCommit() {
        return commitSliderPanel.getStartCommit();
    }

    public Commit getEndCommit() {
        return commitSliderPanel.getEndCommit();
    }

    public interface CommitRangeListener {
        void onCommitRangeChanged(Commit startCommit, Commit endCommit);
    }

    public interface ViewModeListener {
        void onViewModeChanged(DiffViewMode mode);
    }

    public void addCommitRangeListener(CommitRangeListener listener) {
        commitRangeListeners.add(listener);
    }

    public void addViewModeListener(ViewModeListener listener) {
        viewModeListeners.add(listener);
    }

    private void fireCommitRangeChanged(Commit startCommit, Commit endCommit) {
        for (CommitRangeListener listener : commitRangeListeners) {
            listener.onCommitRangeChanged(startCommit, endCommit);
        }
    }

    private void fireViewModeChanged(DiffViewMode mode) {
        for (ViewModeListener listener : viewModeListeners) {
            listener.onViewModeChanged(mode);
        }
    }



    /**
     * Custom slider panel with two thumbs for selecting commit range
     */
    private class CommitSliderPanel extends ThemedPanel {
        @Serial
        private static final long serialVersionUID = 1L;

        private transient List<Commit> commits = new ArrayList<>();
        private int startIndex = 0;
        private int endIndex = 0;
        private int dragThumb = -1;
        private final int THUMB_RADIUS = themeManager.scale(6);
        private final int TRACK_HEIGHT = themeManager.scale(3);
        private final int MARGIN = themeManager.scale(20);

        private ThemedWindow tooltipWindow;
        private ThemedLabel tooltipLabel;

        public CommitSliderPanel() {
            setupMouseListeners();
            createTooltipWindow();
        }

        private void createTooltipWindow() {
            tooltipWindow = new ThemedWindow();

            tooltipLabel = new ThemedLabel();
            tooltipLabel.setFont(new Font("Segoe UI", Font.PLAIN, themeManager.scale(11)));

            tooltipWindow.setContent(tooltipLabel);
        }

        private void setupMouseListeners() {
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (commits.isEmpty()) return;

                    int startThumbX = getThumbX(startIndex);
                    int endThumbX = getThumbX(endIndex);
                    int thumbY = getHeight() / 2 + themeManager.scale(5);

                    if (isNearPoint(e.getX(), e.getY(), startThumbX, thumbY, THUMB_RADIUS)) {
                        dragThumb = 0;
                        showTooltipForIndex(startIndex);
                    } else if (isNearPoint(e.getX(), e.getY(), endThumbX, thumbY, THUMB_RADIUS)) {
                        dragThumb = 1;
                        showTooltipForIndex(endIndex);
                    } else {
                        int index = getIndexFromX(e.getX());
                        showTooltipForIndex(index);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragThumb != -1 && !commits.isEmpty()) {
                        fireCommitRangeChanged(commits.get(startIndex), commits.get(endIndex));
                    }
                    dragThumb = -1;
                    hideTooltip();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragThumb == -1 || commits.isEmpty()) return;

                    int newIndex = getIndexFromX(e.getX());

                    if (dragThumb == 0) {
                        startIndex = Math.max(0, Math.min(newIndex, endIndex - 1));
                        showTooltipForIndex(startIndex);
                    } else {
                        endIndex = Math.max(startIndex + 1, Math.min(newIndex, commits.size() - 1));
                        showTooltipForIndex(endIndex);
                    }

                    repaint();
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void showTooltipForIndex(int index) {
            if (commits.isEmpty()) return;

            index = Math.max(0, Math.min(index, commits.size() - 1));
            Commit commit = commits.get(index);

            String tooltipText = String.format(
                "<html><b>%s</b><br/>%s<br/><i>%s - %s</i></html>",
                commit.getHash().substring(0, Math.min(7, commit.getHash().length())),
                commit.getMessage(),
                commit.getAuthor(),
                commit.getDate()
            );
            tooltipLabel.setText(tooltipText);

            tooltipWindow.pack();

            Point locationOnScreen = getLocationOnScreen();
            int thumbX = getThumbX(index);
            int thumbY = getHeight() / 2 + themeManager.scale(5);

            int tooltipX = locationOnScreen.x + thumbX - tooltipWindow.getWidth() / 2;
            int tooltipY = locationOnScreen.y + thumbY - tooltipWindow.getHeight() - themeManager.scale(10);

            tooltipWindow.showAt(tooltipX, tooltipY);
        }

        private void hideTooltip() {
            if (tooltipWindow != null) {
                tooltipWindow.setVisible(false);
            }
        }


        private boolean isNearPoint(int x, int y, int centerX, int centerY, int radius) {
            int dx = x - centerX;
            int dy = y - centerY;
            return Math.sqrt(dx * dx + dy * dy) <= radius + 5;
        }

        private int getThumbX(int index) {
            if (commits.size() <= 1) return MARGIN;
            int trackWidth = getWidth() - 2 * MARGIN;
            return MARGIN + (index * trackWidth) / (commits.size() - 1);
        }

        private int getIndexFromX(int x) {
            if (commits.size() <= 1) return 0;
            int trackWidth = getWidth() - 2 * MARGIN;
            int index = Math.round((float) (x - MARGIN) * (commits.size() - 1) / trackWidth);
            return Math.max(0, Math.min(index, commits.size() - 1));
        }

        public void setCommits(List<Commit> commits) {
            this.commits = new ArrayList<>(commits);
            this.startIndex = 0;
            this.endIndex = Math.max(0, commits.size() - 1);
            repaint();
        }

        public void setStartIndex(int index) {
            this.startIndex = Math.max(0, Math.min(index, commits.size() - 1));
            repaint();
        }

        public void setEndIndex(int index) {
            this.endIndex = Math.max(0, Math.min(index, commits.size() - 1));
            repaint();
        }

        public Commit getStartCommit() {
            if (commits.isEmpty() || startIndex >= commits.size()) return null;
            return commits.get(startIndex);
        }

        public Commit getEndCommit() {
            if (commits.isEmpty() || endIndex >= commits.size()) return null;
            return commits.get(endIndex);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (commits.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Theme theme = themeManager.getCurrentTheme();
            int width = getWidth();
            int height = getHeight();
            int centerY = height / 2 + themeManager.scale(5);
            int trackWidth = width - 2 * MARGIN;

            g2.setFont(new Font("Consolas", Font.PLAIN, themeManager.scale(9)));
            FontMetrics fm = g2.getFontMetrics();

            int[] visibleIndices = getVisibleCommitIndices();
            for (int i : visibleIndices) {
                if (i >= 0 && i < commits.size()) {
                    int x = getThumbX(i);
                    String hash = commits.get(i).getHash().substring(0, Math.min(6, commits.get(i).getHash().length()));
                    int textWidth = fm.stringWidth(hash);

                    g2.setColor(theme.getSecondaryTextColor());
                    g2.drawString(hash, x - textWidth / 2, centerY - themeManager.scale(12));

                    g2.setColor(theme.getBorderColor());
                    g2.fillOval(x - 2, centerY - TRACK_HEIGHT / 2 - 2, 4, 4);
                }
            }

            g2.setColor(theme.getBorderColor());
            g2.fillRoundRect(MARGIN, centerY - TRACK_HEIGHT / 2, trackWidth, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            int startX = getThumbX(startIndex);
            int endX = getThumbX(endIndex);
            g2.setColor(theme.getAccentColor());
            g2.fillRoundRect(startX, centerY - TRACK_HEIGHT / 2, endX - startX, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            drawThumb(g2, startX, centerY, startIndex == dragThumb, theme);
            drawThumb(g2, endX, centerY, endIndex == dragThumb, theme);

            g2.dispose();
        }

        private int[] getVisibleCommitIndices() {
            if (commits.size() <= 3) {
                int[] indices = new int[commits.size()];
                for (int i = 0; i < commits.size(); i++) {
                    indices[i] = i;
                }
                return indices;
            } else if (commits.size() <= 10) {
                return new int[]{0, commits.size() / 2, commits.size() - 1};
            } else {
                return new int[]{0, commits.size() - 1};
            }
        }

        private void drawThumb(Graphics2D g2, int x, int y, boolean isDragging, Theme theme) {
            int radius = isDragging ? THUMB_RADIUS + 2 : THUMB_RADIUS;

            g2.setColor(theme.getBackgroundColor());
            g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            g2.setColor(theme.getAccentColor());
            g2.setStroke(new BasicStroke(themeManager.scale(2)));
            g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            if (isDragging) {
                g2.setColor(new Color(theme.getAccentColor().getRed(),
                                      theme.getAccentColor().getGreen(),
                                      theme.getAccentColor().getBlue(), 80));
                g2.fillOval(x - radius - 3, y - radius - 3, (radius + 3) * 2, (radius + 3) * 2);
            }
        }
    }
}
