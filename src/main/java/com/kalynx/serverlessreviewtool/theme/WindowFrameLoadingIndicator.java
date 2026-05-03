package com.kalynx.serverlessreviewtool.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Visual loading indicator that renders a traveling segment animation around the window border.
 * Uses theme-aware colors (warm orange for dark theme, blue for light theme).
 */
public class WindowFrameLoadingIndicator extends JComponent {

    private static final int BORDER_THICKNESS = 4;
    private static final int SEGMENT_LENGTH_PERCENT = 20;
    private static final int ANIMATION_DELAY_MS = 16;
    private static final int ROTATION_DURATION_MS = 2500;

    private final Timer animationTimer;
    private float animationProgress = 0f;
    private boolean isAnimating = false;

    /**
     * Constructs a new WindowFrameLoadingIndicator.
     */
    public WindowFrameLoadingIndicator() {
        setOpaque(false);
        animationTimer = new Timer(ANIMATION_DELAY_MS, e -> updateAnimation());
    }

    /**
     * Starts the loading animation.
     */
    public void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animationProgress = 0f;
            animationTimer.start();
        }
    }

    /**
     * Stops the loading animation.
     */
    public void stopAnimation() {
        if (isAnimating) {
            isAnimating = false;
            animationTimer.stop();
            repaint();
        }
    }

    private void updateAnimation() {
        animationProgress += (float) ANIMATION_DELAY_MS / ROTATION_DURATION_MS;
        if (animationProgress >= 1f) {
            animationProgress = 0f;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isAnimating) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            float perimeter = 2 * (width + height);
            float segmentLength = perimeter * SEGMENT_LENGTH_PERCENT / 100f;

            float startPosition = perimeter * animationProgress;
            float endPosition = startPosition + segmentLength;

            Color baseColor = ThemeManager.getInstance().getCurrentTheme() instanceof DarkTheme
                ? new Color(41, 128, 185)
                : new Color(230, 126, 34);

            drawSegment(g2, width, height, startPosition, endPosition, baseColor);
        } finally {
            g2.dispose();
        }
    }

    private void drawSegment(Graphics2D g2, int width, int height, float start, float end, Color baseColor) {
        float perimeter = 2 * (width + height);

        start = start % perimeter;
        end = end % perimeter;

        if (end < start) {
            drawSegmentPortion(g2, width, height, start, perimeter, baseColor);
            drawSegmentPortion(g2, width, height, 0, end, baseColor);
        } else {
            drawSegmentPortion(g2, width, height, start, end, baseColor);
        }
    }

    private void drawSegmentPortion(Graphics2D g2, int width, int height, float start, float end, Color baseColor) {
        Path2D path = new Path2D.Float();
        boolean pathStarted = false;

        float topLength = width;
        float rightLength = topLength + height;
        float bottomLength = rightLength + width;
        float leftLength = bottomLength + height;

        if (start < topLength && end > 0) {
            float x1 = Math.max(0, start);
            float x2 = Math.min(topLength, end);
            if (!pathStarted) {
                path.moveTo(x1, 0);
                pathStarted = true;
            }
            path.lineTo(x2, 0);
        }

        if (start < rightLength && end > topLength) {
            float y1 = Math.max(0, start - topLength);
            float y2 = Math.min(height, end - topLength);
            if (!pathStarted) {
                path.moveTo(width, y1);
                pathStarted = true;
            } else {
                path.lineTo(width, y1);
            }
            path.lineTo(width, y2);
        }

        if (start < bottomLength && end > rightLength) {
            float x1 = Math.max(0, start - rightLength);
            float x2 = Math.min(width, end - rightLength);
            if (!pathStarted) {
                path.moveTo(width - x1, height);
                pathStarted = true;
            } else {
                path.lineTo(width - x1, height);
            }
            path.lineTo(width - x2, height);
        }

        if (start < leftLength && end > bottomLength) {
            float y1 = Math.max(0, start - bottomLength);
            float y2 = Math.min(height, end - bottomLength);
            if (!pathStarted) {
                path.moveTo(0, height - y1);
                pathStarted = true;
            } else {
                path.lineTo(0, height - y1);
            }
            path.lineTo(0, height - y2);
        }

        g2.setStroke(new BasicStroke(BORDER_THICKNESS * 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 40));
        g2.draw(path);

        g2.setStroke(new BasicStroke(BORDER_THICKNESS * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100));
        g2.draw(path);

        g2.setStroke(new BasicStroke(BORDER_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 220));
        g2.draw(path);
    }
}





