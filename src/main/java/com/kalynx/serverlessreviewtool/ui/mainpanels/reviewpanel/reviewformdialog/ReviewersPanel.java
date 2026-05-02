package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
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
    private transient final ComponentModel<List<String>> selectedReviewersModel;
    private transient boolean updatingFromModel = false;

    public ReviewersPanel(ComponentModel<List<String>> availableReviewersModel,
                         ComponentModel<List<String>> selectedReviewersModel) {
        this.selectedReviewers = new ArrayList<>();
        this.selectedReviewersModel = selectedReviewersModel;

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

        reviewerSelector = new ThemedSearchableComboBox(new ArrayList<>());
        reviewerSelector.bindTo(availableReviewersModel);
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

        bindToSelectedModel(selectedReviewersModel);
    }


    private void bindToSelectedModel(ComponentModel<List<String>> selectedReviewersModel) {
        selectedReviewersModel.addChangeListener(reviewers -> {
            updatingFromModel = true;
            selectedReviewers.clear();
            if (reviewers != null) {
                selectedReviewers.addAll(reviewers);
            }
            updateBadges();
            updatingFromModel = false;
        });

        if (selectedReviewersModel.getValue() != null) {
            selectedReviewers.addAll(selectedReviewersModel.getValue());
            updateBadges();
        }
    }

    private void addReviewer(String reviewer) {
        if (!selectedReviewers.contains(reviewer)) {
            selectedReviewers.add(reviewer);
            updateBadges();
            if (!updatingFromModel) {
                selectedReviewersModel.setValue(new ArrayList<>(selectedReviewers));
            }
        }
    }

    private void removeReviewer(String reviewer) {
        selectedReviewers.remove(reviewer);
        updateBadges();
        if (!updatingFromModel) {
            selectedReviewersModel.setValue(new ArrayList<>(selectedReviewers));
        }
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


    public boolean hasSelection() {
        return !selectedReviewers.isEmpty();
    }
}

