package com.kalynx.serverlessreviewtool.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * WindowResizeHandler - Enables window resizing by dragging edges and corners
 * for undecorated windows (windows without OS title bar)
 */
public class WindowResizeHandler extends MouseAdapter {

    private final JFrame frame;
    private final int borderWidth;

    private Point startPos = null;
    private Dimension startSize = null;
    private Point startLocation = null;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;

    private enum ResizeDirection {
        NONE,
        NORTH, SOUTH, EAST, WEST,
        NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
    }

    public WindowResizeHandler(JFrame frame, int borderWidth) {
        this.frame = frame;
        this.borderWidth = borderWidth;
    }

    public WindowResizeHandler(JFrame frame) {
        this(frame, 5); // Default 5px border
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPos = e.getLocationOnScreen();
        startSize = frame.getSize();
        startLocation = frame.getLocation();
        resizeDirection = getResizeDirection(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        resizeDirection = ResizeDirection.NONE;
        startPos = null;
        startSize = null;
        startLocation = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPos == null || resizeDirection == ResizeDirection.NONE) {
            return;
        }

        Point currentPos = e.getLocationOnScreen();
        int deltaX = currentPos.x - startPos.x;
        int deltaY = currentPos.y - startPos.y;

        int newX = startLocation.x;
        int newY = startLocation.y;
        int newWidth = startSize.width;
        int newHeight = startSize.height;

        // Calculate new position and size based on resize direction
        switch (resizeDirection) {
            case NORTH:
                newY = startLocation.y + deltaY;
                newHeight = startSize.height - deltaY;
                break;
            case SOUTH:
                newHeight = startSize.height + deltaY;
                break;
            case EAST:
                newWidth = startSize.width + deltaX;
                break;
            case WEST:
                newX = startLocation.x + deltaX;
                newWidth = startSize.width - deltaX;
                break;
            case NORTH_EAST:
                newY = startLocation.y + deltaY;
                newHeight = startSize.height - deltaY;
                newWidth = startSize.width + deltaX;
                break;
            case NORTH_WEST:
                newY = startLocation.y + deltaY;
                newHeight = startSize.height - deltaY;
                newX = startLocation.x + deltaX;
                newWidth = startSize.width - deltaX;
                break;
            case SOUTH_EAST:
                newHeight = startSize.height + deltaY;
                newWidth = startSize.width + deltaX;
                break;
            case SOUTH_WEST:
                newHeight = startSize.height + deltaY;
                newX = startLocation.x + deltaX;
                newWidth = startSize.width - deltaX;
                break;
        }

        // Enforce minimum size
        Dimension minSize = frame.getMinimumSize();
        if (newWidth < minSize.width) {
            newWidth = minSize.width;
            if (resizeDirection == ResizeDirection.WEST ||
                resizeDirection == ResizeDirection.NORTH_WEST ||
                resizeDirection == ResizeDirection.SOUTH_WEST) {
                newX = startLocation.x + startSize.width - minSize.width;
            }
        }
        if (newHeight < minSize.height) {
            newHeight = minSize.height;
            if (resizeDirection == ResizeDirection.NORTH ||
                resizeDirection == ResizeDirection.NORTH_EAST ||
                resizeDirection == ResizeDirection.NORTH_WEST) {
                newY = startLocation.y + startSize.height - minSize.height;
            }
        }

        // Apply new bounds
        frame.setBounds(newX, newY, newWidth, newHeight);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ResizeDirection direction = getResizeDirection(e);
        updateCursor(direction);
    }

    /**
     * Determine which edge or corner the mouse is near
     * Converts component-relative coordinates to frame-relative coordinates
     */
    private ResizeDirection getResizeDirection(MouseEvent e) {
        // Get mouse position relative to the frame
        Point pointOnScreen = e.getLocationOnScreen();
        Point frameLocation = frame.getLocationOnScreen();

        // Convert to frame-relative coordinates
        int x = pointOnScreen.x - frameLocation.x;
        int y = pointOnScreen.y - frameLocation.y;

        int width = frame.getWidth();
        int height = frame.getHeight();

        boolean north = y < borderWidth;
        boolean south = y > height - borderWidth;
        boolean east = x > width - borderWidth;
        boolean west = x < borderWidth;

        // Corners take precedence
        if (north && west) return ResizeDirection.NORTH_WEST;
        if (north && east) return ResizeDirection.NORTH_EAST;
        if (south && west) return ResizeDirection.SOUTH_WEST;
        if (south && east) return ResizeDirection.SOUTH_EAST;

        // Edges
        if (north) return ResizeDirection.NORTH;
        if (south) return ResizeDirection.SOUTH;
        if (east) return ResizeDirection.EAST;
        if (west) return ResizeDirection.WEST;

        return ResizeDirection.NONE;
    }

    /**
     * Update cursor based on resize direction
     */
    private void updateCursor(ResizeDirection direction) {
        Cursor cursor;

        switch (direction) {
            case NORTH:
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                break;
            case SOUTH:
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                break;
            case EAST:
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                break;
            case WEST:
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                break;
            case NORTH_EAST:
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                break;
            case NORTH_WEST:
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                break;
            case SOUTH_EAST:
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                break;
            case SOUTH_WEST:
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                break;
            default:
                cursor = Cursor.getDefaultCursor();
                break;
        }

        frame.setCursor(cursor);
    }
}


