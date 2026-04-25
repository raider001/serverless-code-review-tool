package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.icons.CloseIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * ThemedPopupDialog - A theme-aware popup dialog with custom title bar
 * Provides a consistent look with the main application
 */
public class ThemedPopupDialog extends JDialog {
    private final ThemeManager themeManager;
    private Point initialClick;
    private JPanel contentPanel;
    private JPanel titleBarPanel;

    public ThemedPopupDialog(Component parent, String title) {
        super(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        this.themeManager = ThemeManager.getInstance();

        // Remove default decorations
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        // Create main panel
        JPanel mainPanel = new ThemedPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create title bar
        titleBarPanel = createTitleBar(title);
        mainPanel.add(titleBarPanel, BorderLayout.NORTH);

        // Create content panel
        contentPanel = new ThemedPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setSize(350, 200);

        // Add shadow border effect
        mainPanel.setBorder(BorderFactory.createLineBorder(
                themeManager.getCurrentTheme().getBorderColor(), 1
        ));

        // Position dialog at parent location
        if (parent != null) {
            Point parentLocation = parent.getLocationOnScreen();
            setLocation(
                    parentLocation.x + parent.getWidth() / 2 - getWidth() / 2,
                    parentLocation.y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
    }

    private JPanel createTitleBar(String title) {
        Theme theme = themeManager.getCurrentTheme();
        ThemedPanel titleBar = new ThemedPanel();
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(0, 40));
        titleBar.setBackground(theme.getBackgroundColor());

        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(theme.getForegroundColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Close button
        QuickButton closeBtn = new QuickButton(new CloseIcon())
                .setTooltip("Close");
        closeBtn.addActionListener(e -> dispose());

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        // Add dragging capability
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;

                    int xMoved = e.getLocationOnScreen().x - (thisX + initialClick.x);
                    int yMoved = e.getLocationOnScreen().y - (thisY + initialClick.y);

                    setLocation(thisX + xMoved, thisY + yMoved);
                }
            }
        });

        return titleBar;
    }

    /**
     * Get the content panel for adding dialog content
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Set dialog size
     */
    public void setDialogSize(int width, int height) {
        setSize(width, height);
    }
}


