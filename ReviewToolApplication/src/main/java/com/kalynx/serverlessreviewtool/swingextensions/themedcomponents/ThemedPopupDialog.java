package com.kalynx.serverlessreviewtool.swingextensions.themedcomponents;

import java.io.Serial;

import com.kalynx.serverlessreviewtool.theme.LoadingStateManager;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.WindowFrameLoadingIndicator;
import com.kalynx.serverlessreviewtool.theme.WindowResizeHandler;

import javax.swing.*;
import java.awt.*;

public class ThemedPopupDialog extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final ThemeManager themeManager;
    private final JPanel contentPanel;
    private final JPanel mainPanel;
    private WindowFrameLoadingIndicator loadingIndicator;

    public ThemedPopupDialog(Component parent, String title) {
        super(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
        this.themeManager = ThemeManager.getInstance();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        mainPanel = new ThemedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(new CustomTitleBar(this, title), BorderLayout.NORTH);

        contentPanel = new ThemedPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.setBorder(BorderFactory.createLineBorder(
                themeManager.getCurrentTheme().getBorderColor(), 1));

        setContentPane(mainPanel);
        setSize(350, 200);

        Point parentLocation = parent.getLocationOnScreen();
        setLocation(
                parentLocation.x + parent.getWidth()  / 2 - getWidth()  / 2,
                parentLocation.y + parent.getHeight() / 2 - getHeight() / 2
        );

        setupLoadingIndicator();
    }

    private void setupLoadingIndicator() {
        loadingIndicator = new WindowFrameLoadingIndicator();
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.setLayout(new BorderLayout());
        glassPane.add(loadingIndicator, BorderLayout.CENTER);
        glassPane.setOpaque(false);
        glassPane.setVisible(true);

        LoadingStateManager.getInstance().addListener(() -> {
            if (LoadingStateManager.getInstance().isLoading()) {
                loadingIndicator.startAnimation();
            } else {
                loadingIndicator.stopAnimation();
            }
        });
    }

    /**
     * Get the content panel for adding dialog content.
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Set dialog size.
     */
    public void setDialogSize(int width, int height) {
        setSize(width, height);
    }

    /**
     * Enable or disable user-resizing by dragging the dialog edges.
     * Uses the same {@link WindowResizeHandler} pattern as {@code ThemedFrame},
     * keeping the dialog undecorated (no OS title bar).
     * Must be called before the dialog is made visible.
     */
    public void setUserResizable(boolean resizable) {
        if (resizable) {
            setMinimumSize(new Dimension(themeManager.scale(400), themeManager.scale(300)));
            WindowResizeHandler resizeHandler = new WindowResizeHandler(this, themeManager.scale(8));
            mainPanel.addMouseListener(resizeHandler);
            mainPanel.addMouseMotionListener(resizeHandler);
            contentPanel.addMouseListener(resizeHandler);
            contentPanel.addMouseMotionListener(resizeHandler);
        }
    }
}
