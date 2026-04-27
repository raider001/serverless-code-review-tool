package com.kalynx.serverlessreviewtool.theme.components;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;

/**
 * A custom themed validation error overlay that appears below form fields
 * This overlay floats above the layout without affecting positioning of other components
 */
public class ThemedValidationOverlay extends JPanel {

    private final ThemeManager themeManager;
    private final JLabel errorLabel;
    private final JComponent targetComponent;

    public ThemedValidationOverlay(JComponent targetComponent) {
        this.themeManager = ThemeManager.getInstance();
        this.targetComponent = targetComponent;

        setLayout(null); // Absolute positioning
        setOpaque(false);

        errorLabel = new JLabel();
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, themeManager.scale(11)));

        add(errorLabel);
        setVisible(false);

        // Update position when target component moves or resizes
        targetComponent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (isVisible()) {
                    updatePosition();
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (isVisible()) {
                    updatePosition();
                }
            }
        });

        // Attach to the layered pane automatically when target is added to hierarchy
        targetComponent.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (targetComponent.isShowing() && getParent() == null) {
                    attachToLayeredPane();
                }
            }
        });
    }

    /**
     * Attaches the overlay to the window's layered pane for proper floating behavior
     */
    private void attachToLayeredPane() {
        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(targetComponent);
            if (window instanceof RootPaneContainer) {
                JRootPane rootPane = ((RootPaneContainer) window).getRootPane();
                JLayeredPane layeredPane = rootPane.getLayeredPane();
                layeredPane.add(this, JLayeredPane.POPUP_LAYER);
                System.out.println("DEBUG: Overlay attached to layered pane");
            }
        });
    }

    /**
     * Shows the error message
     *
     * @param message The error message to display
     */
    public void showError(String message) {
        System.out.println("DEBUG: showError() called with message: " + message);
        errorLabel.setText(message);

        // Ensure we're attached to the layered pane
        if (getParent() == null) {
            attachToLayeredPane();
        }

        updatePosition();
        setVisible(true);
        System.out.println("DEBUG: Overlay bounds: " + getBounds());
        System.out.println("DEBUG: Overlay visible: " + isVisible());
        System.out.println("DEBUG: Overlay parent: " + getParent());

        repaint();
    }

    /**
     * Hides the error message
     */
    public void hideError() {
        setVisible(false);
        repaint();
    }

    /**
     * Updates the position of the overlay to appear below the target component
     */
    private void updatePosition() {
        if (targetComponent == null || !targetComponent.isShowing()) {
            return;
        }

        Container parent = getParent();
        if (parent == null) {
            return;
        }

        // Calculate preferred size based on error message
        Dimension labelSize = errorLabel.getPreferredSize();
        int width = labelSize.width + themeManager.scale(8);
        int height = labelSize.height + themeManager.scale(4);

        // Convert target component's position to layered pane coordinate space
        Point targetLocationOnScreen = targetComponent.getLocationOnScreen();
        Point parentLocationOnScreen = parent.getLocationOnScreen();

        int x = targetLocationOnScreen.x - parentLocationOnScreen.x;
        int y = targetLocationOnScreen.y - parentLocationOnScreen.y + targetComponent.getHeight() + themeManager.scale(2);

        setBounds(x, y, width, height);
        errorLabel.setBounds(
            themeManager.scale(4),
            themeManager.scale(2),
            labelSize.width,
            labelSize.height
        );

        System.out.println("DEBUG: updatePosition() set bounds to: " + getBounds());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background with slight transparency
        Color errorBg = new Color(255, 100, 100, 30);
        g2.setColor(errorBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), themeManager.scale(4), themeManager.scale(4));

        // Draw border
        g2.setColor(new Color(220, 50, 50));
        g2.setStroke(new BasicStroke(themeManager.scale(1)));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, themeManager.scale(4), themeManager.scale(4));

        // Set text color
        errorLabel.setForeground(new Color(200, 40, 40));

        g2.dispose();
    }

}

