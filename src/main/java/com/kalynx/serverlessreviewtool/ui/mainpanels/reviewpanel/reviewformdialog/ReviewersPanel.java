package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewersPanel extends ThemedPanel {

    private final ThemedSearchableComboBox reviewerSelector = new ThemedSearchableComboBox(new ArrayList<>());
    private final ThemedButton addButton = new ThemedButton("Add");
    private final ThemedPanel badgesPanel = new ThemedPanel();

    private final ComponentModel<List<String>> selectedReviewersModel;
    private final ComponentModel<List<String>> availableReviewersModel;

    public ReviewersPanel(ComponentModel<List<String>> availableReviewersModel,
                         ComponentModel<List<String>> selectedReviewersModel) {
        this.availableReviewersModel = availableReviewersModel;
        this.selectedReviewersModel = selectedReviewersModel;

        configureLayout();
        setupBindings();
    }

    private void configureLayout() {
        setLayout(new MigLayout(
            "",
            "[grow,fill][]",
            "[]6[]"
        ));
        setBorder(ThemedTitledBorder.create("Reviewers"));
        badgesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badgesPanel.setOpaque(false);
        ThemedScrollPane badgeScroll = new ThemedScrollPane(badgesPanel);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int badgeRowH = themeManager.scale(34) + badgeScroll.getHorizontalScrollBar().getPreferredSize().height;
        badgeScroll.setPreferredSize(new Dimension(0, badgeRowH));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        add(badgeScroll, "growx, span, wrap");

        reviewerSelector.setToolTipText("Search to add reviewers…");
        addButton.setPreferredSize(new Dimension(themeManager.scale(70), themeManager.scale(28)));
        add(reviewerSelector, "growx");
        add(addButton, "");
    }

    private void setupBindings() {
        reviewerSelector.bindTo(availableReviewersModel);

        selectedReviewersModel.addChangeListener(ignored -> updateBadges());

        updateBadges();

        reviewerSelector.setOnApply(item -> {
            if (item != null && !item.trim().isEmpty()) {
                BindingLifecycleHelper.addToBadgeList(selectedReviewersModel, item);
                reviewerSelector.setSelectedIndex(-1);
            }
        });

        addButton.addActionListener(ignored -> {
            Object selected = reviewerSelector.getSelectedItem();
            if (selected != null && !selected.toString().trim().isEmpty()) {
                BindingLifecycleHelper.addToBadgeList(selectedReviewersModel, selected.toString());
                reviewerSelector.setSelectedIndex(-1);
            }
        });
    }

    private void updateBadges() {
        badgesPanel.removeAll();
        List<String> selected = selectedReviewersModel.getValue();
        if (selected != null) {
            for (String item : selected) {
                badgesPanel.add(new ThemedBadge(item, () ->
                    BindingLifecycleHelper.removeFromBadgeList(selectedReviewersModel, item)));
            }
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

}

