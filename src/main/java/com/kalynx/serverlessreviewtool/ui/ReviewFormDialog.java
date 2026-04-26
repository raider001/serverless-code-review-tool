package com.kalynx.serverlessreviewtool.ui;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.components.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewFormDialog – abstract base shared by CreateReviewDialog and EditReviewDialog.
 *
 * Subclasses must implement:
 *   {@link #getSubmitButtonLabel()} – label for the primary action button
 *   {@link #onFormSubmit()}         – called after validation passes
 *
 * Pre-population hook: subclasses may call {@link #populateFrom} after {@code super(...)}
 * returns to fill the form from an existing ReviewContext.
 */
public abstract class ReviewFormDialog extends ThemedPopupDialog {

    // ---- Layout constants ---------------------------------------------------
    protected static final int DIALOG_W  = 880;
    protected static final int DIALOG_H  = 720;
    protected static final int GAP       = 12;
    protected static final int INSET     = 16;
    protected static final int FIELD_H   = 28;
    protected static final int LIST_H    = 150;
    protected static final int SUMMARY_H = 120;

    protected final ThemeManager themeManager;
    protected boolean confirmed = false;

    // ---- Form controls (protected so subclasses can pre-populate) -----------
    protected ThemedRadioButton branchModeRadio;
    protected ThemedRadioButton commitModeRadio;
    protected ThemedTextField   titleField;
    protected ThemedTextField   authorField;
    protected ThemedTextArea    summaryArea;

    protected ThemedPanel        modeSpecificPanel;
    protected ThemedTextField    branchNameField;
    protected ThemedComboBox<String> reviewAgainstBranchCombo;
    protected ThemedComboBox<String> commitBranchFilterCombo;
    protected ThemedList<String> commitSelectionList;

    protected ThemedTextField repositorySearchField;
    protected ThemedPanel     repositoryCheckboxPanel;
    protected ThemedPanel     repositoryBadgesPanel;
    protected ThemedTextField reviewerSearchField;
    protected ThemedPanel     reviewerCheckboxPanel;
    protected ThemedPanel     reviewerBadgesPanel;

    protected final List<JCheckBox> repositoryCheckboxes = new ArrayList<>();
    protected final List<JCheckBox> reviewerCheckboxes   = new ArrayList<>();
    protected final List<String>    selectedRepositories = new ArrayList<>();
    protected final List<String>    selectedReviewers    = new ArrayList<>();

    // -------------------------------------------------------------------------

    protected ReviewFormDialog(Component parent,
                                String dialogTitle,
                                List<String> availableRepositories,
                                List<String> availableReviewers) {
        super(parent, dialogTitle);
        this.themeManager = ThemeManager.getInstance();

        setDialogSize(themeManager.scale(DIALOG_W), themeManager.scale(DIALOG_H));
        setUserResizable(true);
        initializeContent(availableRepositories, availableReviewers);

        if (parent != null) {
            Point p = parent.getLocationOnScreen();
            setLocation(
                p.x + parent.getWidth()  / 2 - getWidth()  / 2,
                p.y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
    }

    // ── abstract contract ────────────────────────────────────────────────────

    /** Label for the primary action button, e.g. "Create Review" or "Save Changes". */
    protected abstract String getSubmitButtonLabel();

    /** Called when validation passes and the user clicks the primary action button. */
    protected abstract void onFormSubmit();

    // ── root layout ──────────────────────────────────────────────────────────

    private void initializeContent(List<String> repos, List<String> reviewers) {
        ThemedPanel content = (ThemedPanel) getContentPanel();
        content.setLayout(new MigLayout(
            "fill, insets " + INSET + ", gap " + GAP + " " + GAP,
            "[grow]",
            "[]" + GAP + "[]" + GAP + "[grow,fill]" + GAP + "[]"
        ));

        content.add(createDetailsSection(),                                    "grow, wrap");
        content.add(createSourceSection(),                                     "grow, wrap");
        content.add(createSelectionSection(repos, reviewers),                  "grow, wrap");
        content.add(createFooter(),                                            "growx");

        updateModeSpecificPanel();
    }

    // ── 1. Details ───────────────────────────────────────────────────────────

    private ThemedPanel createDetailsSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout(
            "insets 10 12 12 12, gap " + GAP + " 8",
            "[90!][grow,fill][]",
            "[]8[]8[]"
        ));
        section.setBorder(ThemedTitledBorder.create("Review Details"));

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

        titleField = new ThemedTextField(20);
        titleField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        section.add(rightLabel("Title:"));
        section.add(titleField, "growx");
        section.add(modeToggle, "wrap");

        // Row 2: Author
        authorField = new ThemedTextField(20);
        authorField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));
        authorField.setToolTipText("Name of the review author");

        section.add(rightLabel("Author:"));
        section.add(authorField, "growx, span 2, wrap");

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

    // ── 2. Source ────────────────────────────────────────────────────────────

    private ThemedPanel createSourceSection() {
        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout("fill, insets 10 12 12 12", "[grow,fill]", "[grow,fill]"));
        section.setBorder(ThemedTitledBorder.create("Source"));

        modeSpecificPanel = new ThemedPanel();
        modeSpecificPanel.setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));
        section.add(modeSpecificPanel, "grow");
        return section;
    }

    protected void updateModeSpecificPanel() {
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
        panel.setLayout(new MigLayout(
            "insets 0, gap " + GAP + " 8",
            "[100!][grow,fill]" + GAP + "[110!][grow,fill]",
            "[]"
        ));

        branchNameField = new ThemedTextField(20);
        branchNameField.setToolTipText("Enter the branch name to review");
        branchNameField.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        reviewAgainstBranchCombo = new ThemedComboBox<>(
            new String[]{"main", "develop", "staging", "release/v1.0"});
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

        commitBranchFilterCombo = new ThemedComboBox<>(
            new String[]{"All Branches", "main", "develop", "feature/auth", "feature/ui", "release/v1.0"});
        commitBranchFilterCombo.setPreferredSize(new Dimension(0, themeManager.scale(FIELD_H)));

        panel.add(rightLabel("Filter by branch:"));
        panel.add(commitBranchFilterCombo, "growx, wrap");

        DefaultListModel<String> commitListModel = new DefaultListModel<>();
        for (String c : new String[]{
            "abc1234 - Fix authentication issue",
            "def5678 - Add dark mode support",
            "ghi9012 - Update dependencies",
            "jkl3456 - Refactor UI components",
            "mno7890 - Optimize database queries"})
            commitListModel.addElement(c);

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

    // ── 3. Pickers ───────────────────────────────────────────────────────────

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
            f -> repositorySearchField   = f,
            p -> repositoryCheckboxPanel = p,
            b -> repositoryBadgesPanel   = b,
            this::filterRepositories,
            this::updateRepositoryBadges), "grow");

        row.add(buildPickerPanel(
            "Reviewers", "Search reviewers…", reviewers,
            reviewerCheckboxes::addAll,
            f -> reviewerSearchField   = f,
            p -> reviewerCheckboxPanel = p,
            b -> reviewerBadgesPanel   = b,
            this::filterReviewers,
            this::updateReviewerBadges), "grow");

        return row;
    }

    private ThemedPanel buildPickerPanel(
            String title,
            String searchTooltip,
            List<String> items,
            java.util.function.Consumer<List<JCheckBox>> checkboxesSink,
            java.util.function.Consumer<ThemedTextField> searchSink,
            java.util.function.Consumer<ThemedPanel>     listPanelSink,
            java.util.function.Consumer<ThemedPanel>     badgePanelSink,
            Runnable onFilter,
            Runnable onBadgeUpdate) {

        ThemedPanel section = new ThemedPanel();
        section.setLayout(new MigLayout(
            "fill, insets 10 12 12 12, gap 6 6",
            "[grow,fill]",
            "[]6[]6[grow,fill]"
        ));
        section.setBorder(ThemedTitledBorder.create(title));

        // Badges (top)
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

        // Search (below badges)
        ThemedTextField search = new ThemedTextField(20);
        search.setToolTipText(searchTooltip);
        search.setPreferredSize(new Dimension(0, themeManager.scale(24)));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { onFilter.run(); }
            public void removeUpdate(DocumentEvent e)  { onFilter.run(); }
            public void changedUpdate(DocumentEvent e) { onFilter.run(); }
        });
        searchSink.accept(search);
        section.add(search, "growx, wrap");

        // Checklist
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

    // ── 4. Footer ────────────────────────────────────────────────────────────

    private ThemedPanel createFooter() {
        ThemedPanel footer = new ThemedPanel();
        footer.setLayout(new MigLayout("insets 0, gap 8", "[grow,fill][][]", "[]"));
        footer.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(themeManager.getCurrentTheme().getBorderColor());

        ThemedButton cancelBtn = new ThemedButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        ThemedButton submitBtn = new ThemedButton(getSubmitButtonLabel());
        submitBtn.setAccentStyle(true);
        submitBtn.addActionListener(e -> handleSubmit());

        footer.add(sep, "growx, wrap, gapbottom 10, span");
        footer.add(Box.createGlue(), "growx");
        footer.add(cancelBtn);
        footer.add(submitBtn);
        return footer;
    }

    // ── badge helpers ────────────────────────────────────────────────────────

    private ThemedLabel rightLabel(String text) {
        ThemedLabel l = new ThemedLabel(text);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    protected void updateRepositoryBadges() {
        rebuildBadges(repositoryBadgesPanel, repositoryCheckboxes, this::updateRepositoryBadges);
    }

    protected void updateReviewerBadges() {
        rebuildBadges(reviewerBadgesPanel, reviewerCheckboxes, this::updateReviewerBadges);
    }

    protected void rebuildBadges(ThemedPanel panel, List<JCheckBox> source, Runnable refresh) {
        panel.removeAll();
        for (JCheckBox cb : source) {
            if (cb.isSelected()) {
                panel.add(new ThemedBadge(cb.getText(), () -> {
                    cb.setSelected(false);
                    refresh.run();
                }));
            }
        }
        panel.revalidate();
        panel.repaint();
        Container p = panel.getParent();
        while (p != null) { p.revalidate(); p.repaint(); p = p.getParent(); }
    }

    // ── filter ───────────────────────────────────────────────────────────────

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
            if (cb.getText().toLowerCase().contains(q)) panel.add(cb, "wrap");
        }
        panel.revalidate();
        panel.repaint();
    }

    // ── validation + submit ──────────────────────────────────────────────────

    private void handleSubmit() {
        if (titleField.getText().trim().isEmpty()) {
            warn("Please enter a title for the review"); return;
        }
        if (branchModeRadio.isSelected()) {
            if (branchNameField.getText().trim().isEmpty()) {
                warn("Please enter a branch name to review"); return;
            }
            if (reviewAgainstBranchCombo.getSelectedItem() == null) {
                warn("Please select a branch to review against"); return;
            }
        } else if (commitSelectionList.getSelectedValuesList().isEmpty()) {
            warn("Please select at least one commit"); return;
        }
        if (repositoryCheckboxes.stream().noneMatch(JCheckBox::isSelected)) {
            warn("Please select at least one repository"); return;
        }
        if (reviewerCheckboxes.stream().noneMatch(JCheckBox::isSelected)) {
            warn("Please select at least one reviewer"); return;
        }

        selectedRepositories.clear();
        for (JCheckBox cb : repositoryCheckboxes)
            if (cb.isSelected()) selectedRepositories.add(cb.getText());

        selectedReviewers.clear();
        for (JCheckBox cb : reviewerCheckboxes)
            if (cb.isSelected()) selectedReviewers.add(cb.getText());

        onFormSubmit();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── public API ───────────────────────────────────────────────────────────

    public boolean      isConfirmed()            { return confirmed; }
    public boolean      isBranchMode()           { return branchModeRadio.isSelected(); }
    public String       getReviewTitle()         { return titleField.getText(); }
    public String       getAuthor()              { return authorField.getText(); }
    public String       getSummary()             { return summaryArea.getText(); }
    public String       getBranchName()          { return branchNameField.getText(); }
    public String       getReviewAgainstBranch() { return (String) reviewAgainstBranchCombo.getSelectedItem(); }
    public String       getSelectedCommit()      { return commitSelectionList.getSelectedValue(); }
    public List<String> getSelectedCommits()     { return new ArrayList<>(commitSelectionList.getSelectedValuesList()); }
    public String       getSelectedBranchFilter(){ return (String) commitBranchFilterCombo.getSelectedItem(); }
    public List<String> getSelectedRepositories(){ return new ArrayList<>(selectedRepositories); }
    public List<String> getSelectedReviewers()   { return new ArrayList<>(selectedReviewers); }
}







