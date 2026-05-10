package com.kalynx.serverlessreviewtool.utils;

import com.kalynx.serverlessreviewtool.ui.mainpanels.LogsPanel;

import java.io.PrintStream;

/**
 * Bridges standard output and error streams to the logs panel.
 */
public final class ConsoleLogBridge {

    private static boolean installed;
    private static TeeOutputStream teeOut;
    private static TeeOutputStream teeErr;

    private ConsoleLogBridge() {
    }

    /**
     * Installs output stream interception once for the process.
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        teeOut = new TeeOutputStream(originalOut, null);
        teeErr = new TeeOutputStream(originalErr, null);

        System.setOut(new PrintStream(teeOut, true));
        System.setErr(new PrintStream(teeErr, true));
        installed = true;
    }

    /**
     * Attaches the logs panel target for already-installed console interception.
     *
     * @param logsPanel logs panel to receive captured output
     */
    public static synchronized void attachLogsPanel(LogsPanel logsPanel) {
        install();
        teeOut.setLogsPanel(logsPanel);
        teeErr.setLogsPanel(logsPanel);
    }
}

