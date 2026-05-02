package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel.reviewformdialog.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class ReviewFormDialog extends ThemedPopupDialog {

    private static final int DIALOG_W = 880;
    private static final int DIALOG_H = 720;
    private static final int GAP = 12;
    private static final int INSET = 16;

    protected final ThemeManager themeManager;
    protected boolean confirmed = false;

    protected final ReviewDetailsPanel detailsPanel;
    protected final SourcePanel sourcePanel;
    protected final RepositoriesPanel repositoriesPanel;
    protected final ReviewersPanel reviewersPanel;

    protected ReviewFormDialog(Component parent,
                                String dialogTitle,
                                List<String> availableRepositories,
                                List<String> availableReviewers) {
        super(parent, dialogTitle);
        this.themeManager = ThemeManager.getInstance();

        this.detailsPanel = new ReviewDetailsPanel();
        this.sourcePanel = new SourcePanel();
        this.repositoriesPanel = new RepositoriesPanel(availableRepositories);
        this.reviewersPanel = new ReviewersPanel(availableReviewers);

        setDialogSize(themeManager.scale(DIALOG_W), themeManager.scale(DIALOG_H));
        setUserResizable(true);

        configureLayout();
        setupListeners();

        if (parent != null) {
            Point p = parent.getLocationOnScreen();
            setLocation(
                p.x + parent.getWidth() / 2 - getWidth() / 2,
                p.y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
    }

    protected abstract String getSubmitButtonLabel();

    protected abstract void onFormSubmit();

    private void configureLayout() {
        ThemedPanel content = (ThemedPanel) getContentPanel();
        content.setLayout(new MigLayout(
            "fill, insets " + INSET + ", gap " + GAP + " " + GAP,
            "[grow]",
            "[]" + GAP + "[]" + GAP + "[grow,fill]" + GAP + "[]"
        ));

        content.add(detailsPanel, "grow, wrap");
        content.add(sourcePanel, "grow, wrap");
        content.add(createSelectionSection(), "grow, wrap");
        content.add(createFooter(), "growx");

        sourcePanel.updateMode(detailsPanel.isBranchMode());
    }

    private void setupListeners() {
        detailsPanel.setOnModeChangeListener(() -> sourcePanel.updateMode(detailsPanel.isBranchMode()));
    }

    private ThemedPanel createSelectionSection() {
        ThemedPanel row = new ThemedPanel();
        row.setLayout(new MigLayout(
            "fill, insets 0, gap " + GAP + " 0",
            "[grow,fill,sg cols]" + GAP + "[grow,fill,sg cols]",
            "[grow,fill]"
        ));

        row.add(repositoriesPanel, "grow");
        row.add(reviewersPanel, "grow");

        return row;
    }

    private ThemedPanel createFooter() {
        ThemedPanel footer = new ThemedPanel();
        footer.setLayout(new MigLayout("insets 0, gap 8", "[grow,fill][][]", "[]"));
        footer.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setForeground(themeManager.getCurrentTheme().getBorderColor());

        ThemedButton cancelBtn = new ThemedButton("Cancel");
        cancelBtn.addActionListener(ignored -> dispose());

        ThemedButton submitBtn = new ThemedButton(getSubmitButtonLabel());
        submitBtn.setAccentStyle(true);
        submitBtn.addActionListener(ignored -> handleSubmit());

        footer.add(sep, "growx, wrap, gapbottom 10, span");
        footer.add(Box.createGlue(), "growx");
        footer.add(cancelBtn);
        footer.add(submitBtn);
        return footer;
    }


    private void handleSubmit() {
        if (detailsPanel.getTitle().trim().isEmpty()) {
            warn("Please enter a title for the review");
            return;
        }
        if (detailsPanel.isBranchMode()) {
            if (sourcePanel.getBranchName().trim().isEmpty()) {
                warn("Please enter a branch name to review");
                return;
            }
            if (sourcePanel.getReviewAgainstBranch() == null) {
                warn("Please select a branch to review against");
                return;
            }
        } else if (!sourcePanel.hasCommitSelection()) {
            warn("Please select at least one commit");
            return;
        }
        if (!repositoriesPanel.hasSelection()) {
            warn("Please select at least one repository");
            return;
        }
        if (!reviewersPanel.hasSelection()) {
            warn("Please select at least one reviewer");
            return;
        }

        onFormSubmit();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isBranchMode() {
        return detailsPanel.isBranchMode();
    }

    public String getReviewTitle() {
        return detailsPanel.getTitle();
    }

    public String getAuthor() {
        return detailsPanel.getAuthor();
    }

    public String getSummary() {
        return detailsPanel.getSummary();
    }

    public String getBranchName() {
        return sourcePanel.getBranchName();
    }

    public String getReviewAgainstBranch() {
        return sourcePanel.getReviewAgainstBranch();
    }

    public String getSelectedCommit() {
        return sourcePanel.getSelectedCommit();
    }

    public List<String> getSelectedCommits() {
        return sourcePanel.getSelectedCommits();
    }

    public String getSelectedBranchFilter() {
        return sourcePanel.getSelectedBranchFilter();
    }

    public List<String> getSelectedRepositories() {
        return repositoriesPanel.getSelectedRepositories();
    }

    public List<String> getSelectedReviewers() {
        return reviewersPanel.getSelectedReviewers();
    }
}







