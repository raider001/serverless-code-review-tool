package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewpanel;

import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedButton;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import net.miginfocom.swing.MigLayout;

/**
 * RejectApprovePanel - Contains action buttons for review approval workflow
 * Provides Request Changes and Approve buttons for review decisions
 */
public class RejectApprovePanel extends ThemedPanel {

    private final ThemedButton requestChangesButton = new ThemedButton("Request Changes");
    private final ThemedButton approveButton = new ThemedButton("Approve");

    public RejectApprovePanel() {
        configureLayout();
        setupButtons();
    }

    private void configureLayout() {
        setLayout(new MigLayout("fillx, insets 0", "[grow][][]", "[]"));

        add(new ThemedPanel(), "growx");
        add(requestChangesButton);
        add(approveButton);
    }

    private void setupButtons() {
        approveButton.setAccentStyle(true);

        requestChangesButton.addActionListener(e -> onRequestChanges());
        approveButton.addActionListener(e -> onApprove());
    }

    private void onRequestChanges() {
        System.out.println("Request Changes clicked");
    }

    private void onApprove() {
        System.out.println("Approve clicked");
    }
}

