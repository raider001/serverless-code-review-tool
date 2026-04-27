package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * A themed confirmation dialog with custom title bar
 */
public class ThemedConfirmDialog extends JDialog {
    
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private boolean confirmed = false;
    
    public ThemedConfirmDialog(Window owner, String title, String message) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initComponents(title, message);
        applyTheme();
    }
    
    private void initComponents(String title, String message) {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow][]"));
        
        // Add border to the entire dialog for window differentiation
        Theme theme = themeManager.getCurrentTheme();
        contentPanel.setBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 1));

        // Custom title bar
        CustomTitleBar titleBar = new CustomTitleBar(this, title);
        contentPanel.add(titleBar, "cell 0 0, grow, wrap");
        
        // Message panel with padding
        ThemedPanel messagePanel = new ThemedPanel();
        messagePanel.setLayout(new MigLayout("fill, insets 20", "[grow]", "[]"));
        ThemedLabel messageLabel = new ThemedLabel(message);
        messagePanel.add(messageLabel, "cell 0 0, grow");
        contentPanel.add(messagePanel, "cell 0 1, grow, wrap");
        
        // Button panel
        ThemedPanel buttonPanel = new ThemedPanel();
        buttonPanel.setLayout(new MigLayout("insets 10 20 20 20", "[grow][]10[]", "[]"));
        
        ThemedButton yesButton = new ThemedButton("Yes");
        ThemedButton noButton = new ThemedButton("No");
        
        yesButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        
        noButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(yesButton, "cell 1 0, width 80!");
        buttonPanel.add(noButton, "cell 2 0, width 80!");
        
        contentPanel.add(buttonPanel, "cell 0 2, grow");
        
        setContentPane(contentPanel);
        pack();
        setSize(400, 180);
        setLocationRelativeTo(getOwner());
    }
    
    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        getContentPane().setBackground(theme.getBackgroundColor());
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Show a confirmation dialog and return the result
     * 
     * @param parent Parent window
     * @param title Dialog title
     * @param message Confirmation message
     * @return true if user confirmed, false otherwise
     */
    public static boolean showConfirmation(Window parent, String title, String message) {
        ThemedConfirmDialog dialog = new ThemedConfirmDialog(parent, title, message);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }
}


