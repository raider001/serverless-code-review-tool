package com.kalynx.serverlessreviewtool.eventlisteners;

import java.util.List;

public interface SetOnFilterEvent {

    public void setFilterEventAction( String title, String author, List<String> repositories);
}
