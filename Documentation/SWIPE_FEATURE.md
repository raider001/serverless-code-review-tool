# Swipe Gesture Feature

## Overview
The Review Panel now includes a fun swipe gesture feature for approving or requesting changes on code reviews, similar to mobile app interactions. The panels use theme-aware colors that automatically adapt to light and dark themes.

## How It Works

### Activation
- **Only available when you are a reviewer** on an open code review
- The feature is automatically enabled when you join a review
- It's disabled when you're not a reviewer (view-only mode)

### Using the Swipe Gesture

#### Left Side - Request Changes
1. Move your cursor to the **left edge** of the code panel (within 20 pixels)
2. The cursor will change to a resize cursor (⬅)
3. A subtle red indicator with ✕ icon appears (40px peek)
4. **Click and drag to the right** to pull out the "Request Changes" panel
5. The panel expands showing icon, text, progress bar, and percentage (up to 75% of screen width)
6. **Drag at least 90% of the full width** to trigger the action
7. When ready, you'll see "Release!" text pulsing
8. Release the mouse to complete the action

#### Right Side - Approve
1. Move your cursor to the **right edge** of the code panel (within 20 pixels)
2. The cursor will change to a resize cursor (➡)
3. A subtle green indicator with ✓ icon appears (40px peek)
4. **Click and drag to the left** to pull out the "Approve" panel
5. The panel expands showing icon, text, progress bar, and percentage (up to 75% of screen width)
6. **Drag at least 90% of the full width** to trigger the action
7. When ready, you'll see "Release!" text pulsing
8. Release the mouse to complete the action

### Visual Feedback
- **Semi-transparent overlay**: Panels use AlphaComposite for transparency
  - Hover peek: 80% maximum opacity with smooth fade in/out
  - Drag state: Full panel visibility at 80% opacity
  - Fade animation: ~500ms smooth transition (0.08 per 16ms frame)
- **Fade Animations**:
  - **Fade In**: When cursor enters edge zone, panel gradually fades from 0% to 80% opacity
  - **Fade Out**: When cursor leaves edge zone, panel smoothly fades to 0% opacity  
  - **60 FPS**: Buttery smooth 16ms frame rate for fluid animations
  - **Independent**: Left and right panels animate independently
- **Left panel (Request Changes)**: Red/pink gradient background (theme-aware)
- **Right panel (Approve)**: Green gradient background (theme-aware)
- **Icon styling**: Black circular backgrounds with white symbols for both X and checkmark
- **Full-height display**: Panels extend from top to bottom of the window
- **Edge-aligned**: Panels pull from the absolute edge of the screen
- **Hover state (40px peek)**: 
  - Circular badge (36px) with black background and subtle drop shadow
  - White X cross or white checkmark icon
  - Centered in narrow panel
  - Fades in smoothly when cursor enters edge zone
- **Drag state (up to 75% of screen width)**: 
  - Large circular icon (56px) with black background and drop shadow
  - White geometric shapes (X cross or checkmark path)
  - Fixed position (100px from edge) - no jumping during resize
  - Action text below icon
  - Horizontal progress bar (140px x 6px) with rounded corners
  - Progress percentage
  - "Release to approve/reject" pulsing text when ≥90%
- **Gradients**: 
  - Horizontal gradient from bright to darker shade
  - Applied with alpha compositing for smooth transparency
  - Subtle fade at top and bottom edges (80px)
  - Creates depth while content remains visible underneath
- **Smooth animations**: Fade-out effect on successful completion
- **Theme Integration**: Colors automatically adapt to light/dark theme
  - Dark theme: Bright green (#72E689) for approve, bright red (#F85B67) for changes
  - Icons always black backgrounds with white symbols for consistency

### Relationship to Buttons
- The swipe gesture triggers the **same actions** as the "Request Changes" and "Approve" buttons at the bottom
- Both methods are available - use whichever you prefer!
- The buttons and swipe gestures are enabled/disabled together based on your reviewer status

## Technical Details

### Components
- **SwipeActionPanel**: Wraps the code panel and handles mouse gestures
- **RejectApprovePanel**: Contains the traditional button interface
- Both connect to the same action handlers in ReviewPanel

### Thresholds
- **Edge trigger zone**: 20 pixels from left/right edge
- **Panel width**: 75% of screen width (dynamic)
- **Action threshold**: 90% of panel width (dynamic)
- **Hover peek width**: 40 pixels
- **Fade animation**: 0.08 alpha increment per frame (~500ms total)

### States
- **Disabled**: When not a reviewer (cursor remains normal)
- **Ready**: Cursor near edge (resize cursor appears)
- **Dragging**: Pulling panel out (shows progress)
- **Triggered**: Released past threshold (action fires + animation)

## Architecture

### Component Hierarchy
```
MainFrame
  └─ SwipeActionPanel (wraps entire ReviewPanel)
       └─ ReviewPanel
            ├─ ReviewDetailPanel (header info, buttons)
            ├─ CodePanel (file viewer, diff viewer)
            └─ RejectApprovePanel (approve/request changes buttons)
```

### Design Benefits
- **Simplified**: SwipeActionPanel wraps ReviewPanel at MainFrame level
- **Clean height calculations**: Panels automatically span full window height
- **No offset math**: Positioned at root level eliminates complex parent traversal
- **Separation of concerns**: SwipeActionPanel is presentation, ReviewPanel is logic

### Key Files
- `SwipeActionPanel.java` - Swipe gesture component with theme integration (wraps ReviewPanel)
- `ReviewPanel.java` - Main review logic and layout
- `MainFrame.java` - Wires SwipeActionPanel around ReviewPanel
- `RejectApprovePanel.java` - Button interface with shared actions

### Theme Integration
- Extends `ThemedPanel` for automatic theme updates
- Queries theme colors on-demand during paint
- Uses `Theme.getChangesRequestedColor()` for left panel
- Uses `Theme.getApprovedColor()` for right panel
- Automatically calculates contrasting text color (black/white) based on background luminance
- No theme listeners needed - colors refresh on every repaint

### Action Handlers
```java
private void handleApprove() {
    // Approve the code review
}

private void handleRequestChanges() {
    // Request changes on the code review
}
```

Both the swipe gestures and buttons call these same handlers.

## Future Enhancements
- Haptic feedback (if available)
- Configurable thresholds
- Additional swipe directions (up/down for other actions)
- Custom colors per user preference
- Sound effects
- Undo/confirmation dialog option

















