package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateReviewDialog extends ThemedPopupDialog {

    // ---- Layout constants ---------------------------------------------------
    private static final int DIALOG_W   = 880;
    private static final int DIALOG_H   = 720;
    private static final int GAP        = 12;
    private static final int INSET      = 16;
    private static final int FIELD_H    = 28;
    private static final int LIST_H     = 150;
    private static final int SUMMARY_H  = 70;

    private final ThemeManager themeManager;
    private boolean confirmed = false;

    // ---- Form controls ------------------------------------------------------
    private ThemedRadioButton branchModeRadio;
    private ThemedRadioButton commitModeRadio;
    private ThemedTextField titleField;
    private ThemedTextArea summaryArea;

    // Mode-specific
    private ThemedPanel modeSpecificPanel;
    private ThemedTextField branchNameField;
    private ThemedComboBox<String> reviewAgainstBranchCombo;
    private ThemedComboBox<String> commitBranchFilterCombo;
    private ThemedList<String> commitSelectionList;
    private DefaultListModel<String> commitListModel;

    // Pickers
    private ThemedTextField repositorySearchField;
    private ThemedPanel repositoryCheckboxPanel;
    private ThemedPanel repositoryBadgesPanel;
    private ThemedTextField reviewerSearchField;
    private ThemedPanel reviewerCheckboxPanel;
    private ThemedPanel reviewerBadgesPanel;

    private final List<JCheckBox> repositoryCheckboxes = new ArrayList<>();
    private final List<JCheckBox> reviewerCheckboxes   = new ArrayList<>();
    private final List<String> selectedRepositories    = new ArrayList<>();
    private final List<String> selectedReviewers       = new ArrayList<>();

    public CreateReviewDialog(Component parent, List<String> availableRepositories) {
        this(parent, availableRepositories, new ArrayList<>());
    }

    public CreateReviewDialog(Component parent,
                              List<String> availableRepositories,
                              List<String> availableReviewers) {
        super(parent, "Create Code Review");
        this.themeManager = ThemeManager.getInstance();

        setDialogSize(themeManager.scale(DIALOG_W), themeManager.scale(DIALOG_H));
        setUserResizable(true);
        initializeContent(availableRepositories, availableReviewers);

        // Re-center after resizing
        if (parent != null) {
            Point p = parent.getLocationOnScreen();
            setLocation(
                p.x + parent.getWidth()  / 2 - getWidth()  / 2,
                p.y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
    }

    // ============================================================ root layout
    private void initializeContent(List<String> availableRepositories,
                                   List<String> availableReviewers) {
        ThemedPanel content = (ThemedPanel) getContentPanel();
        content.setLayout(new MigLayout(
            "fill, insets " + INSET + ", gap " + GAP + " " + GAP,
            "[grow,grow]",
            "[]" + GAP + "[]" + GAP + "[grow,fill]" + GAP + "[]"
        ));

        content.add(createDetailsSection(),                "grow, wrap");
        content.add(createSourceSection(),                 "grow, wrap");
        content.add(createSelectionSection(availableRepositories, availableReviewers),
                                                           "grow, wrap");
        content.add(createFooter(),                        "growx");

        updateModeSpecificPanel();
    }

    // ============================================================ 1. Details
    private ThemedPanel createDetailsSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout(
            "insets 10 12 12 12, gap " + GAP + " 8",
            "[90!][grow,fill][]",  // label | field | mode-toggle
            "[]8[]"
        ));
        section.setBorder(ThemedTitledBorder.create("Review Details"));

        // Mode toggle (top-right of section)
        branchModeRadio = new ThemedRadioButton("Branch");
        commitModeRadio = new ThemedRadioButton("Commit");
        branchModeRadio.setSelected(true);
        branchModeRadio.addActionListener(e -> updateModeSpecificPanel());
        commitModeRadio.addActionListener(e -> updateModeSpecificPanel());
        ButtonGroup grp = new ButtonGroup();
        grp.add(branchModeRadio);
        grp.add(commitModeRadio);

        ThemedPanel modeToggle = new ThemedPanel();
        modeToggle.setLayout(new MigLayout("insets 0, gap 4", "[][]", ""));
        modeToggle.setOpaque(false);
        modeToggle.add(new ThemedLabel("Mode:"));
        modeToggle.add(branchModeRadio);
        modeToggle.add(commitModeRadio);

        // Row 1: Title + mode toggle
        titleField = new ThemedTextField(20);
        titleField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        section.add(rightLabel("Title:"));
        section.add(titleField, "growx");
        section.add(modeToggle, "wrap");

        // Row 2: Summary (spans field + toggle columns)
        summaryArea = new ThemedTextArea(3, 40);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        ThemedScrollPane summaryScroll = new ThemedScrollPane(summaryArea);
        summaryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        summaryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setPreferredSize(new Dimension(0, themeManager.scale(SUMMARY_H)));

        section.add(rightLabel("Summary:"), "aligny top, gaptop 4");
        section.add(summaryScroll, "grow, span 2");

        return section;
    }

    // ============================================================ 2. Source
    private ThemedPanel createSourceSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout(
            "fill, insets 10 12 12 12",
            "[grow,fill]",
            "[grow,fill]"
        ));
        section.setBorder(ThemedTitledBorder.create("Source"));

        modeSpecificPanel = new ThemedPanel();
        modeSpecificPanel.setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));
        section.add(modeSpecificPanel, "grow");
        return section;
    }

    private void updateModeSpecificPanel() {
        modeSpecificPanel.removeAll();
        if (branchModeRadio.isSelected()) {
            modeSpecificPanel.add(createBranchModePanel(), "grow");
        } else {
            modeSpecificPanel.add(createCommitModePanel(), "grow");
        }
        modeSpecificPanel.revalidate();
        modeSpecificPanel.repaint();
    }

    private ThemedPanel createBranchModePanel() {
        ThemedPanel panel = new ThemedPanel();
        // Two side-by-side label/field pairs for symmetry
        panel.setLayout(new MigLayout(
            "insets 0, gap " + GAP + " 8",
            "[100!][grow,fill]" + GAP + "[110!][grow,fill]",
            "[]"
        ));

        branchNameField = new ThemedTextField(20);
        branchNameField.setToolTipText("Enter the branch name to review");
        branchNameField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        String[] sampleBranches = {"main", "develop", "staging", "release/v1.0"};
        reviewAgainstBranchCombo = new ThemedComboBox<>(sampleBranches);
        reviewAgainstBranchCombo.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        panel.add(rightLabel("Branch:"));
        panel.add(branchNameField, "growx");
        panel.add(rightLabel("Review against:"));
        panel.add(reviewAgainstBranchCombo, "growx");

        return panel;
    }

    private ThemedPanel createCommitModePanel() {
        ThemedPanel panel = new ThemedPanel();
        panel.setLayout(new MigLayout(
            "fill, insets 0, gap " + GAP + " 8",
            "[100!][grow,fill]",
            "[]8[][grow,fill]"
        ));

        String[] sampleBranches =
            {"All Branches", "main", "develop", "feature/auth", "feature/ui", "release/v1.0"};
        commitBranchFilterCombo = new ThemedComboBox<>(sampleBranches);
        commitBranchFilterCombo.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        panel.add(rightLabel("Filter by branch:"));
        panel.add(commitBranchFilterCombo, "growx, wrap");

        String[] sampleCommits = {
            "abc1234 - Fix authentication issue",
            "def5678 - Add dark mode support",
            "ghi9012 - Update dependencies",
            "jkl3456 - Refactor UI components",
            "mno7890 - Optimize database queries"
        };
        commitListModel = new DefaultListModel<>();
        for (String c : sampleCommits) commitListModel.addElement(c);

        commitSelectionList = new ThemedList<>(commitListModel);
        commitSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        commitSelectionList.setVisibleRowCount(4);

        ThemedScrollPane scroll = new ThemedScrollPane(commitSelectionList);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(rightLabel("Commits:"), "aligny top, gaptop 4");
        panel.add(scroll, "grow");

        return panel;
    }

    // ============================================================ 3. Pickers
    private ThemedPanel createSelectionSection(List<String> repos, List<String> reviewers) {
        ThemedPanel row = new ThemedPanel();
        row.setLayout(new MigLayout(
            "fill, insets 0, gap " + GAP + " 0",
            "[grow,fill,sg cols]" + GAP + "[grow,fill,sg cols]",
            "[grow,fill]"
        ));

        row.add(buildPickerPanel(
            "Repositories", "Search repositories…", repos,
                repositoryCheckboxes::addAll,
            f -> repositorySearchField    = f,
            p -> repositoryCheckboxPanel  = p,
            b -> repositoryBadgesPanel    = b,
            this::filterRepositories,
            this::updateRepositoryBadges), "grow");


        row.add(createReviewerPanel(reviewers,
            this::filterReviewers,
            this::updateReviewerBadges), "grow");

        return row;
    }

    private ThemedPanel createReviewerPanel(List<String> items, Runnable onFilter, Runnable onBadgeUpdate) {
        ThemedPanel row = new ThemedPanel();
        row.setLayout(new MigLayout(
            "fill, insets 10 12 12 12, gap 6 6",
            "[grow,fill]",
            "[]6[]6[grow,fill]"
        ));
        row.setBorder(ThemedTitledBorder.create("Reviewers"));

        ThemedTextField search = new ThemedTextField(20);
        search.setPreferredSize(new Dimension(0, themeManager.scale(24)));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onFilter.run(); }
            public void removeUpdate(DocumentEvent e) { onFilter.run(); }
            public void changedUpdate(DocumentEvent e) { onFilter.run(); }
        });
        reviewerSearchField = search;

        ThemedPanel badgePanel = new ThemedPanel();
        badgePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badgePanel.setOpaque(false);
        reviewerBadgesPanel = badgePanel;
        ThemedScrollPane badgeScroll = new ThemedScrollPane(badgePanel);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int badgeRowH = themeManager.scale(34) + badgeScroll.getHorizontalScrollBar().getPreferredSize().height;
        badgeScroll.setPreferredSize(new Dimension(0, badgeRowH));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        row.add(badgeScroll, "growx, wrap");
        row.add(search, "growx, wrap");

        ThemedPanel list = new ThemedPanel();
        list.setLayout(new MigLayout("insets 4 4 4 4, gap 0 2", "[grow,fill]", ""));
        list.setOpaque(false);
        List<JCheckBox> created = new ArrayList<>();
        for (String item : items) {
            ThemedCheckBox cb = new ThemedCheckBox(item);
            cb.addActionListener(e -> onBadgeUpdate.run());
            created.add(cb);
            list.add(cb, "wrap");
        }
        reviewerCheckboxes.addAll(created);
        reviewerCheckboxPanel = list;

        ThemedScrollPane scroll = new ThemedScrollPane(list);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, themeManager.scale(LIST_H)));
        row.add(scroll, "grow");

        return row;
    }


    /** Generic picker panel: titled border > search > badges > scrollable checklist. */
    private ThemedPanel buildPickerPanel(
            String title,
            String searchTooltip,
            List<String> items,
            java.util.function.Consumer<List<JCheckBox>> checkboxesSink,
            java.util.function.Consumer<ThemedTextField> searchSink,
            java.util.function.Consumer<ThemedPanel> listPanelSink,
            java.util.function.Consumer<ThemedPanel> badgePanelSink,
            Runnable onFilter,
            Runnable onBadgeUpdate) {

        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout(
            "fill, insets 10 12 12 12, gap 6 6",
            "[grow,fill]",
            "[]6[]6[grow,fill]"
        ));
        section.setBorder(ThemedTitledBorder.create(title));

        // 1) Search
        ThemedTextField search = new ThemedTextField(20);
        search.setToolTipText(searchTooltip);
        search.setPreferredSize(new Dimension(0, themeManager.scale(24)));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onFilter.run(); }
            public void removeUpdate(DocumentEvent e) { onFilter.run(); }
            public void changedUpdate(DocumentEvent e) { onFilter.run(); }
        });
        searchSink.accept(search);

        // 2) Badges – single scrollable row with horizontal scrollbar
        ThemedPanel badges = new ThemedPanel();
        badges.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badges.setOpaque(false);
        badgePanelSink.accept(badges);
        ThemedScrollPane badgeScroll = new ThemedScrollPane(badges);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        int badgeRowH = themeManager.scale(34) + badgeScroll.getHorizontalScrollBar().getPreferredSize().height;
        badgeScroll.setPreferredSize(new Dimension(0, badgeRowH));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        section.add(badgeScroll, "growx, wrap");

        // 1) Search (below badges)
        section.add(search, "growx, wrap");

        // 3) Checklist
        ThemedPanel list = new ThemedPanel();
        list.setLayout(new MigLayout("insets 4 4 4 4, gap 0 2", "[grow,fill]", ""));
        list.setOpaque(false);
        List<JCheckBox> created = new ArrayList<>();
        for (String item : items) {
            ThemedCheckBox cb = new ThemedCheckBox(item);
            cb.addActionListener(e -> onBadgeUpdate.run());
            created.add(cb);
            list.add(cb, "wrap");
        }
        checkboxesSink.accept(created);
        listPanelSink.accept(list);

        ThemedScrollPane scroll = new ThemedScrollPane(list);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, themeManager.scale(LIST_H)));
        section.add(scroll, "grow");

        return section;
    }

    // ============================================================ 4. Footer
    private ThemedPanel createFooter() {
        ThemedPanel footer = new ThemedPanel();
        footer.setLayout(new MigLayout(
            "insets 0, gap 8",
            "[grow,fill][][]",
            "[]"
        ));
        footer.setOpaque(false);

        // A subtle separator above the action row
        JSeparator sep = new JSeparator();
        sep.setForeground(themeManager.getCurrentTheme().getBorderColor());

        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        ThemedButton createButton = new ThemedButton("Create Review");
        createButton.setAccentStyle(true);
        createButton.addActionListener(e -> handleCreate());

        footer.add(sep, "growx, wrap, gapbottom 10, span");
        footer.add(Box.createGlue(), "growx");
        footer.add(cancelButton);
        footer.add(createButton);

        return footer;
    }

    // ============================================================ Badges
    private ThemedLabel rightLabel(String text) {
        ThemedLabel l = new ThemedLabel(text);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private void updateRepositoryBadges() {
        rebuildBadges(repositoryBadgesPanel, repositoryCheckboxes, this::updateRepositoryBadges);
    }

    private void updateReviewerBadges() {
        rebuildBadges(reviewerBadgesPanel, reviewerCheckboxes, this::updateReviewerBadges);
    }

    private void rebuildBadges(ThemedPanel panel, List<JCheckBox> source, Runnable refresh) {
        panel.removeAll();

        for (JCheckBox cb : source) {
            if (cb.isSelected()) {
                ThemedBadge badge = new ThemedBadge(cb.getText(), () -> {
                    cb.setSelected(false);
                    refresh.run();
                });
                panel.add(badge);  // FlowLayout — no constraints needed
            }
        }

        // Invalidate up the hierarchy so MigLayout re-measures the badges row height
        panel.revalidate();
        panel.repaint();
        Container parent = panel.getParent();
        while (parent != null) {
            parent.revalidate();
            parent.repaint();
            parent = parent.getParent();
        }
    }

    // ============================================================ Filter
    private void filterRepositories() {
        applyFilter(repositorySearchField, repositoryCheckboxPanel, repositoryCheckboxes);
    }

    private void filterReviewers() {
        applyFilter(reviewerSearchField, reviewerCheckboxPanel, reviewerCheckboxes);
    }

    private void applyFilter(ThemedTextField search, ThemedPanel panel, List<JCheckBox> all) {
        String q = search.getText().toLowerCase().trim();
        panel.removeAll();
        for (JCheckBox cb : all) {
            if (cb.getText().toLowerCase().contains(q)) {
                panel.add(cb, "wrap");
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    // ============================================================ Submit
    private void handleCreate() {
        if (titleField.getText().trim().isEmpty()) {
            warn("Please enter a title for the review");
            return;
        }
        if (branchModeRadio.isSelected()) {
            if (branchNameField.getText().trim().isEmpty()) {
                warn("Please enter a branch name to review");
                return;
            }
            if (reviewAgainstBranchCombo.getSelectedItem() == null) {
                warn("Please select a branch to review against");
                return;
            }
        } else if (commitSelectionList.getSelectedValuesList().isEmpty()) {
            warn("Please select at least one commit");
            return;
        }
        if (repositoryCheckboxes.stream().noneMatch(JCheckBox::isSelected)) {
            warn("Please select at least one repository");
            return;
        }
        if (reviewerCheckboxes.stream().noneMatch(JCheckBox::isSelected)) {
            warn("Please select at least one reviewer");
            return;
        }

        selectedRepositories.clear();
        for (JCheckBox cb : repositoryCheckboxes) {
            if (cb.isSelected()) selectedRepositories.add(cb.getText());
        }
        selectedReviewers.clear();
        for (JCheckBox cb : reviewerCheckboxes) {
            if (cb.isSelected()) selectedReviewers.add(cb.getText());
        }

        confirmed = true;
        dispose();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    // ============================================================ Public API
    public boolean isConfirmed()              { return confirmed; }
    public boolean isBranchMode()             { return branchModeRadio.isSelected(); }
    public String  getReviewTitle()           { return titleField.getText(); }
    public String  getSummary()               { return summaryArea.getText(); }
    public String  getBranchName()            { return branchNameField.getText(); }
    public String  getReviewAgainstBranch()   { return (String) reviewAgainstBranchCombo.getSelectedItem(); }
    public String  getSelectedCommit()        { return commitSelectionList.getSelectedValue(); }
    public List<String> getSelectedCommits()  { return new ArrayList<>(commitSelectionList.getSelectedValuesList()); }
    public String  getSelectedBranchFilter()  { return (String) commitBranchFilterCombo.getSelectedItem(); }
    public List<String> getSelectedRepositories() { return new ArrayList<>(selectedRepositories); }
    public List<String> getSelectedReviewers()    { return new ArrayList<>(selectedReviewers); }
}













