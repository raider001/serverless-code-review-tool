package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.ui.review.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CommitSelectorPanel - Allows selection of commit range to compare
 */
public class CommitSelectorPanel extends ThemedPanel {

    private final ThemeManager themeManager;
    private ReviewContext reviewContext;
    private ThemedComboBox<Repository> repositoryComboBox;
    private ThemedComboBox<Commit> startCommitComboBox;
    private ThemedComboBox<Commit> endCommitComboBox;
    private ThemedComboBox<DiffViewMode> viewModeComboBox;
    private final List<CommitSelectionListener> listeners;
    private final List<ViewModeListener> viewModeListeners;

    public CommitSelectorPanel(ReviewContext reviewContext) {
        this.themeManager = ThemeManager.getInstance();
        this.reviewContext = reviewContext;
        this.listeners = new ArrayList<>();
        this.viewModeListeners = new ArrayList<>();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            ThemedTitledBorder.create("Commit Selection"),
            BorderFactory.createEmptyBorder(
                themeManager.scale(5),
                themeManager.scale(8),
                themeManager.scale(5),
                themeManager.scale(8)
            )
        ));

        initializeComponents();
    }

    private void initializeComponents() {
        // Repository selector
        add(new ThemedLabel("Repository:"));
        add(Box.createHorizontalStrut(themeManager.scale(5)));

        repositoryComboBox = new ThemedComboBox<>();
        repositoryComboBox.setMaximumSize(new Dimension(themeManager.scale(150), themeManager.scale(30)));
        repositoryComboBox.setPreferredSize(new Dimension(themeManager.scale(150), themeManager.scale(25)));
        repositoryComboBox.addActionListener(e -> onRepositoryChanged());
        add(repositoryComboBox);
        add(Box.createHorizontalStrut(themeManager.scale(15)));

        // Start commit selector
        add(new ThemedLabel("From:"));
        add(Box.createHorizontalStrut(themeManager.scale(5)));

        startCommitComboBox = new ThemedComboBox<>();
        startCommitComboBox.setMaximumSize(new Dimension(themeManager.scale(250), themeManager.scale(30)));
        startCommitComboBox.setPreferredSize(new Dimension(themeManager.scale(250), themeManager.scale(25)));
        startCommitComboBox.addActionListener(e -> onCommitSelectionChanged());
        add(startCommitComboBox);
        add(Box.createHorizontalStrut(themeManager.scale(15)));

        // End commit selector
        add(new ThemedLabel("To:"));
        add(Box.createHorizontalStrut(themeManager.scale(5)));

        endCommitComboBox = new ThemedComboBox<>();
        endCommitComboBox.setMaximumSize(new Dimension(themeManager.scale(250), themeManager.scale(30)));
        endCommitComboBox.setPreferredSize(new Dimension(themeManager.scale(250), themeManager.scale(25)));
        endCommitComboBox.addActionListener(e -> onCommitSelectionChanged());
        add(endCommitComboBox);

        add(Box.createHorizontalStrut(themeManager.scale(15)));

        // View mode selector
        add(new ThemedLabel("View Mode:"));
        add(Box.createHorizontalStrut(themeManager.scale(5)));

        viewModeComboBox = new ThemedComboBox<>(DiffViewMode.values());
        viewModeComboBox.setMaximumSize(new Dimension(themeManager.scale(150), themeManager.scale(30)));
        viewModeComboBox.setPreferredSize(new Dimension(themeManager.scale(150), themeManager.scale(25)));
        viewModeComboBox.addActionListener(e -> onViewModeChanged());
        add(viewModeComboBox);

        add(Box.createHorizontalGlue());

        // Load data
        loadRepositories();
    }

    private void loadRepositories() {
        repositoryComboBox.removeAllItems();
        for (Repository repo : reviewContext.getRepositories()) {
            repositoryComboBox.addItem(repo);
        }

        if (repositoryComboBox.getItemCount() > 0) {
            repositoryComboBox.setSelectedIndex(0);
            onRepositoryChanged();
        }
    }

    private void onRepositoryChanged() {
        Repository selectedRepo = (Repository) repositoryComboBox.getSelectedItem();
        if (selectedRepo == null) return;

        // Load commits for this repository
        startCommitComboBox.removeAllItems();
        endCommitComboBox.removeAllItems();

        List<Commit> commits = selectedRepo.getCommits();
        for (Commit commit : commits) {
            startCommitComboBox.addItem(commit);
            endCommitComboBox.addItem(commit);
        }

        // Default: Select first commit as start, last commit as end
        if (commits.size() > 0) {
            startCommitComboBox.setSelectedIndex(0);
            endCommitComboBox.setSelectedIndex(commits.size() - 1);
        }
    }

    private void onCommitSelectionChanged() {
        Commit startCommit = (Commit) startCommitComboBox.getSelectedItem();
        Commit endCommit = (Commit) endCommitComboBox.getSelectedItem();

        if (startCommit != null && endCommit != null) {
            fireCommitSelectionChanged(startCommit, endCommit);
        }
    }

    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        loadRepositories();
    }

    public Commit getStartCommit() {
        return (Commit) startCommitComboBox.getSelectedItem();
    }

    public Commit getEndCommit() {
        return (Commit) endCommitComboBox.getSelectedItem();
    }

    public Repository getSelectedRepository() {
        return (Repository) repositoryComboBox.getSelectedItem();
    }

    // Listener interface
    public interface CommitSelectionListener {
        void onCommitSelectionChanged(Commit startCommit, Commit endCommit);
    }

    public void addCommitSelectionListener(CommitSelectionListener listener) {
        listeners.add(listener);
    }

    private void fireCommitSelectionChanged(Commit startCommit, Commit endCommit) {
        for (CommitSelectionListener listener : listeners) {
            listener.onCommitSelectionChanged(startCommit, endCommit);
        }
    }

    private void onViewModeChanged() {
        DiffViewMode selectedMode = (DiffViewMode) viewModeComboBox.getSelectedItem();
        if (selectedMode != null) {
            fireViewModeChanged(selectedMode);
        }
    }

    public DiffViewMode getSelectedViewMode() {
        return (DiffViewMode) viewModeComboBox.getSelectedItem();
    }

    // View Mode Listener interface
    public interface ViewModeListener {
        void onViewModeChanged(DiffViewMode mode);
    }

    public void addViewModeListener(ViewModeListener listener) {
        viewModeListeners.add(listener);
    }

    private void fireViewModeChanged(DiffViewMode mode) {
        for (ViewModeListener listener : viewModeListeners) {
            listener.onViewModeChanged(mode);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Recreate titled border with current theme colors
        // TitledBorders are immutable and must be recreated to pick up new theme colors
        setBorder(BorderFactory.createCompoundBorder(
            ThemedTitledBorder.create("Commit Selection"),
            BorderFactory.createEmptyBorder(
                themeManager.scale(5),
                themeManager.scale(8),
                themeManager.scale(5),
                themeManager.scale(8)
            )
        ));
        super.paintComponent(g);
    }
}

