package com.kalynx.serverlessreviewtool.theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/**
 * WindowResizeHandler - Enables window resizing by dragging edges and corners
 * for undecorated windows (windows without OS title bar).
 * Works with any Window subclass (JFrame, JDialog, etc.).
 */
public class WindowResizeHandler extends MouseAdapter {
    private final Window window;
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
    /** Convenience constructor for JFrame (preserves existing call sites). */
    public WindowResizeHandler(JFrame frame, int borderWidth) {
        this((Window) frame, borderWidth);
    }
    /** Constructor for any Window. */
    public WindowResizeHandler(Window window, int borderWidth) {
        this.window      = window;
        this.borderWidth = borderWidth;
    }
    /** Default 5px border width. */
    public WindowResizeHandler(Window window) {
        this(window, 5);
    }
    @Override
    public void mousePressed(MouseEvent e) {
        startPos        = e.getLocationOnScreen();
        startSize       = window.getSize();
        startLocation   = window.getLocation();
        resizeDirection = getResizeDirection(e);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        resizeDirection = ResizeDirection.NONE;
        startPos        = null;
        startSize       = null;
        startLocation   = null;
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPos == null || resizeDirection == ResizeDirection.NONE) return;
        Point currentPos = e.getLocationOnScreen();
        int deltaX = currentPos.x - startPos.x;
        int deltaY = currentPos.y - startPos.y;
        int newX      = startLocation.x;
        int newY      = startLocation.y;
        int newWidth  = startSize.width;
        int newHeight = startSize.height;
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
                newWidth  = startSize.width  + deltaX;
                break;
            case SOUTH_WEST:
                newHeight = startSize.height + deltaY;
                newX      = startLocation.x + deltaX;
                newWidth  = startSize.width  - deltaX;
                break;
            default:
                break;
        }
        Dimension minSize = window.getMinimumSize();
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
        window.setBounds(newX, newY, newWidth, newHeight);
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        updateCursor(getResizeDirection(e));
    }
    private ResizeDirection getResizeDirection(MouseEvent e) {
        Point pointOnScreen  = e.getLocationOnScreen();
        Point windowLocation = window.getLocationOnScreen();
        int x = pointOnScreen.x - windowLocation.x;
        int y = pointOnScreen.y - windowLocation.y;
        int w = window.getWidth();
        int h = window.getHeight();
        boolean north = y < borderWidth;
        boolean south = y > h - borderWidth;
        boolean east  = x > w - borderWidth;
        boolean west  = x < borderWidth;
        if (north && west) return ResizeDirection.NORTH_WEST;
        if (north && east) return ResizeDirection.NORTH_EAST;
        if (south && west) return ResizeDirection.SOUTH_WEST;
        if (south && east) return ResizeDirection.SOUTH_EAST;
        if (north) return ResizeDirection.NORTH;
        if (south) return ResizeDirection.SOUTH;
        if (east)  return ResizeDirection.EAST;
        if (west)  return ResizeDirection.WEST;
        return ResizeDirection.NONE;
    }
    private void updateCursor(ResizeDirection direction) {
        int cursorType;
        switch (direction) {
            case NORTH:      cursorType = Cursor.N_RESIZE_CURSOR;  break;
            case SOUTH:      cursorType = Cursor.S_RESIZE_CURSOR;  break;
            case EAST:       cursorType = Cursor.E_RESIZE_CURSOR;  break;
            case WEST:       cursorType = Cursor.W_RESIZE_CURSOR;  break;
            case NORTH_EAST: cursorType = Cursor.NE_RESIZE_CURSOR; break;
            case NORTH_WEST: cursorType = Cursor.NW_RESIZE_CURSOR; break;
            case SOUTH_EAST: cursorType = Cursor.SE_RESIZE_CURSOR; break;
            case SOUTH_WEST: cursorType = Cursor.SW_RESIZE_CURSOR; break;
            default:         cursorType = Cursor.DEFAULT_CURSOR;   break;
        }
        window.setCursor(Cursor.getPredefinedCursor(cursorType));
    }
}
