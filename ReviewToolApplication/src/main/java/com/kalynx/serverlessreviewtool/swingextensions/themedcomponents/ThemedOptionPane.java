package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class ThemedOptionPane extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager = ThemeManager.getInstance();

    public enum MessageType {
        INFO,
        WARNING,
        ERROR
    }

    public ThemedOptionPane(Window owner, String title, String message, MessageType type) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initComponents(title, message, type);
        positionToRight(owner);
    }

    private void initComponents(String title, String message, MessageType type) {
        ThemedPanel contentPanel = new ThemedPanel();
        contentPanel.setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow][]"));

        Theme theme = themeManager.getCurrentTheme();
        contentPanel.setBorder(BorderFactory.createLineBorder(theme.getBorderColor(), 1));

        CustomTitleBar titleBar = new CustomTitleBar(this, title);
        contentPanel.add(titleBar, "cell 0 0, grow, wrap");

        ThemedPanel messagePanel = new ThemedPanel();
        messagePanel.setLayout(new MigLayout("fill, insets 20", "[]20[grow]", "[]"));

        MessageIcon.MessageType iconType = switch (type) {
            case INFO -> MessageIcon.MessageType.INFO;
            case WARNING -> MessageIcon.MessageType.WARNING;
            case ERROR -> MessageIcon.MessageType.ERROR;
        };

        MessageIcon icon = new MessageIcon(iconType, getIconColor(type));

        ThemedLabel messageLabel = new ThemedLabel("<html><body style='width: 300px'>" + message + "</body></html>");
        messagePanel.add(icon, "cell 0 0, aligny top");
        messagePanel.add(messageLabel, "cell 1 0, grow");

        contentPanel.add(messagePanel, "cell 0 1, grow, wrap");

        ThemedPanel buttonPanel = new ThemedPanel();
        buttonPanel.setLayout(new MigLayout("insets 10 20 20 20", "[grow][]", "[]"));

        ThemedButton okButton = new ThemedButton("OK");
        okButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton, "cell 1 0, width 80!");

        contentPanel.add(buttonPanel, "cell 0 2, grow");

        setContentPane(contentPanel);
        setMinimumSize(new Dimension(themeManager.scale(450), themeManager.scale(180)));
        pack();

        int preferredWidth = themeManager.scale(450);
        int preferredHeight = Math.max(getHeight(), themeManager.scale(180));
        setSize(preferredWidth, preferredHeight);
    }

    private Color getIconColor(MessageType type) {
        return switch (type) {
            case INFO -> new Color(100, 150, 255);
            case WARNING -> new Color(255, 180, 50);
            case ERROR -> new Color(255, 100, 100);
        };
    }

    private void positionToRight(Window owner) {
        if (owner == null) {
            setLocationRelativeTo(null);
            return;
        }

        Point ownerLocation = owner.getLocationOnScreen();
        int ownerWidth = owner.getWidth();
        int ownerHeight = owner.getHeight();

        int dialogX = ownerLocation.x + ownerWidth - getWidth() - themeManager.scale(20);
        int dialogY = ownerLocation.y + (ownerHeight - getHeight()) / 2;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialogX = Math.min(dialogX, screenSize.width - getWidth());
        dialogX = Math.max(dialogX, 0);
        dialogY = Math.max(dialogY, 0);
        dialogY = Math.min(dialogY, screenSize.height - getHeight());

        setLocation(dialogX, dialogY);
    }

    public static void showMessageDialog(Component parent, String message, String title, MessageType type) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        ThemedOptionPane dialog = new ThemedOptionPane(owner, title, message, type);
        dialog.setVisible(true);
    }

    public static void showWarning(Component parent, String message) {
        showMessageDialog(parent, message, "Warning", MessageType.WARNING);
    }

    public static void showError(Component parent, String message) {
        showMessageDialog(parent, message, "Error", MessageType.ERROR);
    }

    public static void showInfo(Component parent, String message) {
        showMessageDialog(parent, message, "Information", MessageType.INFO);
    }
}

