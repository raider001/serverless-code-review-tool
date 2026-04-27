package com.kalynx.serverlessreviewtool.ui.mainpanels.settingspanel;

import com.kalynx.serverlessreviewtool.configuration.AppSettings;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Custom cell renderer for displaying repository configurations in a list
 */
public class RepositoryListCellRenderer extends DefaultListCellRenderer {

    private final ThemeManager themeManager = ThemeManager.getInstance();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof AppSettings.RepositoryConfig) {
            AppSettings.RepositoryConfig config = (AppSettings.RepositoryConfig) value;
            setText("<html><b>" + config.getName() + "</b><br/>" +
                    "<small>" + config.getUrl() + " (Poll: " + config.getPollingIntervalMinutes() + " min)</small></html>");
        }

        Theme theme = themeManager.getCurrentTheme();
        if (isSelected) {
            setBackground(theme.getAccentColor());
            setForeground(Color.WHITE);
        } else {
            setBackground(theme.getBackgroundColor());
            setForeground(theme.getForegroundColor());
        }

        return this;
    }
}

