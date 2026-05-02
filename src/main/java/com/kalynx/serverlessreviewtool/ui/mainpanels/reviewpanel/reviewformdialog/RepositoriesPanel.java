package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.BindingLifecycleHelper;
import com.kalynx.serverlessreviewtool.swingextensions.ComponentModel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RepositoriesPanel extends ThemedPanel {

    private final ThemedSearchableComboBox repositorySelector = new ThemedSearchableComboBox(new ArrayList<>());
    private final ThemedButton addButton = new ThemedButton("Add");
    private final ThemedPanel badgesPanel = new ThemedPanel();

    private final ComponentModel<List<String>> selectedRepositoriesModel;
    private final ComponentModel<List<String>> availableRepositoriesModel;

    public RepositoriesPanel(ComponentModel<List<String>> availableRepositoriesModel,
                            ComponentModel<List<String>> selectedRepositoriesModel) {
        this.availableRepositoriesModel = availableRepositoriesModel;
        this.selectedRepositoriesModel = selectedRepositoriesModel;

        configureLayout();
        setupBindings();
    }

    private void configureLayout() {
        setLayout(new MigLayout(
                "",
                "[grow,fill][]",
                "[]6[]"
        ));
        setBorder(ThemedTitledBorder.create("Repositories"));
        badgesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badgesPanel.setOpaque(false);
        ThemedScrollPane badgeScroll = new ThemedScrollPane(badgesPanel);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int badgeRowH = themeManager.scale(34) + badgeScroll.getHorizontalScrollBar().getPreferredSize().height;
        badgeScroll.setPreferredSize(new Dimension(0, badgeRowH));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        add(badgeScroll, "growx, span, wrap");

        repositorySelector.setToolTipText("Search to add repositories…");
        addButton.setPreferredSize(new Dimension(themeManager.scale(70), themeManager.scale(28)));
        add(repositorySelector, "growx");
        add(addButton, "");
    }

    private void setupBindings() {
        repositorySelector.bindTo(availableRepositoriesModel);

        selectedRepositoriesModel.addChangeListener(ignored -> SwingUtilities.invokeLater(this::updateBadges));

        updateBadges();

        repositorySelector.setOnApply(item -> {
            if (item != null && !item.trim().isEmpty()) {
                BindingLifecycleHelper.addToBadgeList(selectedRepositoriesModel, item);
                repositorySelector.setSelectedIndex(-1);
            }
        });

        addButton.addActionListener(ignored -> {
            Object selected = repositorySelector.getSelectedItem();
            if (selected != null && !selected.toString().trim().isEmpty()) {
                BindingLifecycleHelper.addToBadgeList(selectedRepositoriesModel, selected.toString());
                repositorySelector.setSelectedIndex(-1);
            }
        });
    }

    private void updateBadges() {
        badgesPanel.removeAll();
        List<String> selected = selectedRepositoriesModel.getValue();
        if (selected != null) {
            for (String item : selected) {
                badgesPanel.add(new ThemedBadge(item, () ->
                    BindingLifecycleHelper.removeFromBadgeList(selectedRepositoriesModel, item)));
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

