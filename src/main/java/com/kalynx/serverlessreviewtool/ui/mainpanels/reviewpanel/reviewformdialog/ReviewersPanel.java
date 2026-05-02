package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewersPanel extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemedSearchableComboBox reviewerSelector;
    private final ThemedPanel badgesPanel;
    private transient final List<String> selectedReviewers;

    public ReviewersPanel(List<String> availableReviewers) {
        this.selectedReviewers = new ArrayList<>();

        setLayout(new MigLayout(
            "",
            "[grow,fill][]",
            "[]6[]"
        ));
        setBorder(ThemedTitledBorder.create("Reviewers"));

        badgesPanel = new ThemedPanel();
        badgesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badgesPanel.setOpaque(false);

        ThemedScrollPane badgeScroll = new ThemedScrollPane(badgesPanel);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int badgeRowH = themeManager.scale(34) + badgeScroll.getHorizontalScrollBar().getPreferredSize().height;
        badgeScroll.setPreferredSize(new Dimension(0, badgeRowH));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        add(badgeScroll, "growx, span, wrap");

        reviewerSelector = new ThemedSearchableComboBox(availableReviewers);
        reviewerSelector.setToolTipText("Search to add reviewers…");
        reviewerSelector.setOnApply(item -> {
            if (item != null && !item.trim().isEmpty()) {
                addReviewer(item);
                reviewerSelector.setSelectedIndex(-1);
            }
        });

        ThemedButton addButton = new ThemedButton("Add");
        addButton.setPreferredSize(new Dimension(themeManager.scale(70), themeManager.scale(28)));
        addButton.addActionListener(ignored -> {
            Object selected = reviewerSelector.getSelectedItem();
            if (selected != null && !selected.toString().trim().isEmpty()) {
                addReviewer(selected.toString());
                reviewerSelector.setSelectedIndex(-1);
            }
        });

        add(reviewerSelector, "growx");
        add(addButton, "");
    }

    private void addReviewer(String reviewer) {
        if (!selectedReviewers.contains(reviewer)) {
            selectedReviewers.add(reviewer);
            updateBadges();
        }
    }

    private void removeReviewer(String reviewer) {
        selectedReviewers.remove(reviewer);
        updateBadges();
    }

    private void updateBadges() {
        badgesPanel.removeAll();
        for (String item : selectedReviewers) {
            badgesPanel.add(new ThemedBadge(item, () -> removeReviewer(item)));
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
        Container p = badgesPanel.getParent();
        while (p != null) {
            p.revalidate();
            p.repaint();
            p = p.getParent();
        }
    }

    public List<String> getSelectedReviewers() {
        return new ArrayList<>(selectedReviewers);
    }

    public void setSelectedReviewers(List<String> reviewers) {
        selectedReviewers.clear();
        selectedReviewers.addAll(reviewers);
        updateBadges();
    }

    public boolean hasSelection() {
        return !selectedReviewers.isEmpty();
    }
}

