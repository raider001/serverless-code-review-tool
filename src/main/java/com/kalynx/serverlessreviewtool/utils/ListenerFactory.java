package com.kalynx.serverlessreviewtool.utils;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class ListenerFactory {

    private ListenerFactory() {
    }

    public static FocusAdapter createFocusLostAdapter(Consumer<FocusEvent> consumer) {
        return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                consumer.accept(e);
            }
        };
    }
}

