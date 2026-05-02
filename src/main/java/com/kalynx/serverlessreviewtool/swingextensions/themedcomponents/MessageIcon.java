package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.Serial;

public class MessageIcon extends JComponent {
    @Serial
    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final Color iconColor;
    private transient final ThemeManager themeManager = ThemeManager.getInstance();

    public enum MessageType {
        INFO,
        WARNING,
        ERROR
    }

    public MessageIcon(MessageType type, Color iconColor) {
        this.type = type;
        this.iconColor = iconColor;
        int size = themeManager.scale(40);
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 2;

        switch (type) {
            case INFO -> paintInfoIcon(g2d, centerX, centerY, radius);
            case WARNING -> paintWarningIcon(g2d, centerX, centerY, radius);
            case ERROR -> paintErrorIcon(g2d, centerX, centerY, radius);
        }

        g2d.dispose();
    }

    private void paintInfoIcon(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Ellipse2D circle = new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2);
        g2d.draw(circle);

        int dotSize = radius / 6;
        Ellipse2D dot = new Ellipse2D.Double(centerX - dotSize / 2, centerY - radius / 2 - dotSize, dotSize, dotSize);
        g2d.fill(dot);

        int lineHeight = (int) (radius * 1.1);
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY - radius / 6, centerX, centerY + lineHeight / 2);
    }

    private void paintWarningIcon(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D triangle = new Path2D.Double();
        int triangleHeight = (int) (radius * 1.8);
        int triangleWidth = (int) (radius * 1.6);

        triangle.moveTo(centerX, centerY - triangleHeight / 2);
        triangle.lineTo(centerX - triangleWidth / 2, centerY + triangleHeight / 2);
        triangle.lineTo(centerX + triangleWidth / 2, centerY + triangleHeight / 2);
        triangle.closePath();
        g2d.draw(triangle);

        int lineHeight = radius / 2;
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY - lineHeight / 2, centerX, centerY + lineHeight / 4);

        int dotSize = radius / 6;
        Ellipse2D dot = new Ellipse2D.Double(centerX - dotSize / 2, centerY + lineHeight, dotSize, dotSize);
        g2d.fill(dot);
    }

    private void paintErrorIcon(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Ellipse2D circle = new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2);
        g2d.draw(circle);

        int crossSize = (int) (radius * 0.7);
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2d.drawLine(
            centerX - crossSize / 2, centerY - crossSize / 2,
            centerX + crossSize / 2, centerY + crossSize / 2
        );
        g2d.drawLine(
            centerX + crossSize / 2, centerY - crossSize / 2,
            centerX - crossSize / 2, centerY + crossSize / 2
        );
    }
}

