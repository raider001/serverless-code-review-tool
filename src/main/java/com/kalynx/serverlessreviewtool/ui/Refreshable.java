package com.kalynx.serverlessreviewtool.ui;

/**
 * Refreshable interface for panels that support refresh operations.
 * Panels implementing this interface will have the refresh button visible
 * when they are active in the MainFrame.
 */
public interface Refreshable {
    /**
     * Called when the user clicks the refresh button.
     * Implementations should refresh their data/content as needed.
     */
    void onRefresh();
}

