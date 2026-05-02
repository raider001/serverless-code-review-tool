package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.theme.ScalableComponent;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.icons.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * CustomTitleBar - A theme-aware, draggable custom title bar
 * Replaces the OS-supplied window decoration
 */
public class CustomTitleBar extends ThemedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager;
    private final JLabel titleLabel;
    private final QuickButton themeToggleBtn;
    private ThemedPanel buttonPanel;
    private Point initialClick;
    private final SlideOutMenu slideOutMenu;

    /**
     * Full title bar for a JFrame: hamburger menu, theme toggle, minimize, maximize, close.
     */
    public CustomTitleBar(JFrame parentFrame, String title) {
        this.themeManager = ThemeManager.getInstance();

        setLayout(new BorderLayout());
        setPreferredSize(ScalableComponent.createDimension(800, 40));
        setOpaque(true);

        slideOutMenu = new SlideOutMenu(parentFrame);

        ThemedPanel titlePanel = new ThemedPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        QuickButton hamburgerBtn = new QuickButton(new HamburgerIcon())
                .setAccentHover()
                .setTooltip("Menu");
        hamburgerBtn.setVisible(true);
        hamburgerBtn.setOpaque(true);
        hamburgerBtn.addActionListener(e -> slideOutMenu.toggle());
        titlePanel.add(hamburgerBtn);
        titlePanel.add(Box.createHorizontalStrut(themeManager.scale(10)));

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(13)));
        titleLabel.setForeground(themeManager.getCurrentTheme().getAccentColor());
        titlePanel.add(titleLabel);

        buttonPanel = new ThemedPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        themeToggleBtn = createThemeToggleButton();
        themeToggleBtn.addActionListener(e -> {
            if (themeManager.getCurrentTheme().getName().equals("Dark")) {
                themeManager.setLightTheme();
            } else {
                themeManager.setDarkTheme();
            }
            updateThemeToggleButton();
        });

        QuickButton minimizeBtn = new QuickButton(new MinimizeIcon()).setTooltip("Minimize");
        minimizeBtn.addActionListener(e -> parentFrame.setState(Frame.ICONIFIED));

        QuickButton maximizeBtn = new QuickButton(new MaximizeIcon()).setTooltip("Maximize");
        maximizeBtn.addActionListener(e -> {
            if (parentFrame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                parentFrame.setExtendedState(Frame.NORMAL);
            } else {
                parentFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        });

        QuickButton closeBtn = new QuickButton(new CloseIcon())
                .setCustomHover(new Color(232, 17, 35), Color.WHITE)
                .setTooltip("Close");
        closeBtn.addActionListener(e -> { parentFrame.dispose(); System.exit(0); });

        buttonPanel.add(themeToggleBtn);
        buttonPanel.add(minimizeBtn);
        buttonPanel.add(maximizeBtn);
        buttonPanel.add(closeBtn);

        add(titlePanel, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.EAST);

        makeDraggable(parentFrame);
    }

    /**
     * Compact title bar for a dialog or any Window: title + close only, no menu/min/max.
     */
    public CustomTitleBar(Window window, String title) {
        this.themeManager = ThemeManager.getInstance();
        this.slideOutMenu = null;
        this.themeToggleBtn = null;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, themeManager.scale(40)));
        setOpaque(true);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, themeManager.scale(13)));
        titleLabel.setForeground(themeManager.getCurrentTheme().getForegroundColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, themeManager.scale(10), 0, 0));

        QuickButton closeBtn = new QuickButton(new CloseIcon()).setTooltip("Close");
        closeBtn.addActionListener(_ -> window.dispose());

        add(titleLabel, BorderLayout.WEST);
        add(closeBtn,   BorderLayout.EAST);

        makeDraggable(window);
    }

    private void makeDraggable(Window window) {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int x = window.getLocation().x + e.getX() - initialClick.x;
                    int y = window.getLocation().y + e.getY() - initialClick.y;
                    window.setLocation(x, y);
                }
            }
        });
    }

    /**
     * Create theme toggle button with dynamic sun/moon icon
     */
    private QuickButton createThemeToggleButton() {
        QuickButton button = new QuickButton()
            .setAccentHover()
            .setTooltip(getThemeToggleTooltip());

        // Set dynamic icon painter based on current theme
        updateThemeToggleIconPainter(button);

        return button;
    }

    /**
     * Update theme toggle button icon based on current theme
     */
    private void updateThemeToggleButton() {
        if (themeToggleBtn != null) {
            updateThemeToggleIconPainter(themeToggleBtn);
            themeToggleBtn.setToolTipText(getThemeToggleTooltip());
            themeToggleBtn.repaint();
        }
    }

    /**
     * Update the icon painter for theme toggle button
     */
    private void updateThemeToggleIconPainter(QuickButton button) {
        if (themeManager.getCurrentTheme().getName().equals("Dark")) {
            // Show sun icon (switch to light)
            button.setIconPainter((g2d, width, height, foreground) -> {
                int centerX = width / 2;
                int centerY = height / 2;
                int size = Math.min(width, height) / 4;

                g2d.setColor(foreground);

                // Center circle
                g2d.fillOval(centerX - size/2, centerY - size/2, size, size);

                // Sun rays
                int rayLength = size / 2;
                int rayThickness = Math.max(2, size / 8);
                g2d.setStroke(new BasicStroke(rayThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int startDist = size / 2 + size / 4;
                    int endDist = startDist + rayLength;

                    int x1 = centerX + (int)(Math.cos(angle) * startDist);
                    int y1 = centerY + (int)(Math.sin(angle) * startDist);
                    int x2 = centerX + (int)(Math.cos(angle) * endDist);
                    int y2 = centerY + (int)(Math.sin(angle) * endDist);

                    g2d.drawLine(x1, y1, x2, y2);
                }
            });
        } else {
            // Show moon icon (switch to dark)
            button.setIconPainter((g2d, width, height, foreground) -> {
                int centerX = width / 2;
                int centerY = height / 2;
                int size = Math.min(width, height) / 4;
                int moonSize = (int)(size * 1.3);

                g2d.setColor(foreground);

                // Outer circle (full moon)
                g2d.fillOval(centerX - moonSize/2, centerY - moonSize/2, moonSize, moonSize);

                // Inner circle (to create crescent by removing part)
                g2d.setColor(button.getBackground());
                int offsetX = moonSize / 4;
                g2d.fillOval(centerX - moonSize /2 + offsetX, centerY - moonSize /2, moonSize, moonSize);
            });
        }
    }

    /**
     * Get tooltip text based on current theme
     */
    private String getThemeToggleTooltip() {
        if (themeManager.getCurrentTheme().getName().equals("Dark")) {
            return "Switch to Light Theme";
        } else {
            return "Switch to Dark Theme";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Query theme colors on demand
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.getBorderColor()));

        if (titleLabel != null) {
            titleLabel.setForeground(theme.getAccentColor());
        }

        // Update panel backgrounds
        for (Component comp : getComponents()) {
            if (comp instanceof ThemedPanel) {
                comp.setBackground(theme.getBackgroundColor());
            }
        }

        super.paintComponent(g);
    }

    /**
     * Update the title text
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Get the slide-out menu for customization
     */
    public SlideOutMenu getSlideOutMenu() {
        return slideOutMenu;
    }

    /**
     * Add an action button to the title bar (left of theme toggle)
     * Only available for JFrame title bars (not dialogs)
     */
    public void addActionButton(QuickButton button) {
        if (buttonPanel != null && themeToggleBtn != null) {
            // Count how many components are already in the button panel
            int insertPosition = buttonPanel.getComponentCount() > 0 ? 
                                 buttonPanel.getComponentCount() - 4 : 0; // Insert before theme/min/max/close
            buttonPanel.add(button, insertPosition);
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }
    }
}


