package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RepositoriesPanel extends ThemedPanel {

    private final ThemedSearchableComboBox repositorySelector;
    private final ThemedPanel badgesPanel;
    private final List<String> selectedRepositories;

    public RepositoriesPanel(List<String> availableRepositories) {
        this.selectedRepositories = new ArrayList<>();

        setLayout(new MigLayout(
            "",
            "[grow,fill][]",
            "[]6[]"
        ));
        setBorder(ThemedTitledBorder.create("Repositories"));

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

        repositorySelector = new ThemedSearchableComboBox(availableRepositories);
        repositorySelector.setToolTipText("Search to add repositories…");
        repositorySelector.setOnApply(item -> {
            if (item != null && !item.trim().isEmpty()) {
                addRepository(item);
                repositorySelector.setSelectedIndex(-1);
            }
        });

        ThemedButton addButton = new ThemedButton("Add");
        addButton.setPreferredSize(new Dimension(themeManager.scale(70), themeManager.scale(28)));
        addButton.addActionListener(e -> {
            Object selected = repositorySelector.getSelectedItem();
            if (selected != null && !selected.toString().trim().isEmpty()) {
                addRepository(selected.toString());
                repositorySelector.setSelectedIndex(-1);
            }
        });

        add(repositorySelector, "growx");
        add(addButton, "");
    }

    private void addRepository(String repository) {
        if (!selectedRepositories.contains(repository)) {
            selectedRepositories.add(repository);
            updateBadges();
        }
    }

    private void removeRepository(String repository) {
        selectedRepositories.remove(repository);
        updateBadges();
    }

    private void updateBadges() {
        badgesPanel.removeAll();
        for (String item : selectedRepositories) {
            badgesPanel.add(new ThemedBadge(item, () -> removeRepository(item)));
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

    public List<String> getSelectedRepositories() {
        return new ArrayList<>(selectedRepositories);
    }

    public void setSelectedRepositories(List<String> repositories) {
        selectedRepositories.clear();
        selectedRepositories.addAll(repositories);
        updateBadges();
    }

    public boolean hasSelection() {
        return !selectedRepositories.isEmpty();
    }
}

