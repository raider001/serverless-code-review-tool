package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.Serial;

/**
 * Themed password field with theme-aware colors and borders.
 */
public class ThemedPasswordField extends JPasswordField {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 8;

    private final transient ThemeManager themeManager;
    private boolean valid = true;
    private boolean passwordVisible = false;
    private final char defaultEchoChar;

    /**
     * Creates a themed password field with the specified column count.
     *
     * @param columns the number of columns to use to calculate the preferred width
     */
    public ThemedPasswordField(int columns) {
        super(columns);
        this.themeManager = ThemeManager.getInstance();
        this.defaultEchoChar = getEchoChar();
        initializeField();
    }

    private void initializeField() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isToggleHit(e.getPoint())) {
                    togglePasswordVisibility();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(isToggleHit(e.getPoint())
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        });
    }

    /**
     * Indicates whether this field is currently marked valid.
     *
     * @return true when valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Shows or hides the password text.
     *
     * @param visible true to show the password, false to mask it
     */
    public void setPasswordVisible(boolean visible) {
        this.passwordVisible = visible;
        setEchoChar(visible ? (char) 0 : defaultEchoChar);
        repaint();
    }

    /**
     * Toggles the current password visibility state.
     */
    public void togglePasswordVisibility() {
        setPasswordVisible(!passwordVisible);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Theme theme = themeManager.getCurrentTheme();
        setBackground(theme.getInputBackground());
        setForeground(theme.getForegroundColor());
        setCaretColor(theme.getAccentColor());
        setSelectionColor(theme.getAccentColor());
        setSelectedTextColor(Color.WHITE);

        if (!valid) {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, themeManager.scale(2)),
                createPaddingBorder(themeManager.scale(3), themeManager.scale(6))
            ));
        } else {
            setBorder(createPaddingBorder(themeManager.scale(5), themeManager.scale(8)));
        }

        super.paintComponent(g);

        paintToggleIcon((Graphics2D) g.create(), theme);
    }

    private void paintToggleIcon(Graphics2D g2d, Theme theme) {
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(isEnabled() ? theme.getForegroundColor() : new Color(150, 150, 150));

            int iconX = getIconX();
            int iconY = getIconY();
            drawEyeIcon(g2d, iconX, iconY, themeManager.scale(ICON_SIZE), themeManager.scale(ICON_SIZE), passwordVisible);
        } finally {
            g2d.dispose();
        }
    }

    private boolean isToggleHit(Point point) {
        int scaledIconSize = themeManager.scale(ICON_SIZE);
        int iconLeft = getIconX();
        int iconTop = getIconY();
        return point.x >= iconLeft
            && point.x <= iconLeft + scaledIconSize
            && point.y >= iconTop
            && point.y <= iconTop + scaledIconSize;
    }

    private javax.swing.border.Border createPaddingBorder(int verticalPadding, int horizontalPadding) {
        return BorderFactory.createEmptyBorder(
            verticalPadding,
            horizontalPadding,
            verticalPadding,
            horizontalPadding + themeManager.scale(ICON_SIZE + ICON_GAP * 2)
        );
    }

    private int getIconX() {
        return getWidth() - themeManager.scale(ICON_SIZE + ICON_GAP);
    }

    private int getIconY() {
        return (getHeight() - themeManager.scale(ICON_SIZE)) / 2;
    }

    private void drawEyeIcon(Graphics2D g2d, int x, int y, int width, int height, boolean open) {
        g2d.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width * 0.42;
        double ry = height * 0.27;

        Path2D eye = new Path2D.Double();
        eye.moveTo(cx - rx, cy);
        eye.quadTo(cx - rx * 0.5, cy - ry, cx, cy - ry);
        eye.quadTo(cx + rx * 0.5, cy - ry, cx + rx, cy);
        eye.quadTo(cx + rx * 0.5, cy + ry, cx, cy + ry);
        eye.quadTo(cx - rx * 0.5, cy + ry, cx - rx, cy);
        g2d.draw(eye);

        if (open) {
            double pupil = Math.min(width, height) * 0.16;
            g2d.fill(new Ellipse2D.Double(cx - pupil, cy - pupil, pupil * 2, pupil * 2));
            return;
        }

        g2d.draw(new Line2D.Double(
            x + width * 0.22,
            y + height * 0.78,
            x + width * 0.78,
            y + height * 0.22
        ));
    }
}



