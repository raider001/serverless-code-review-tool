package com.kalynx.serverlessreviewtool.ui.mainpanels.reviewselectionpanel;

import com.kalynx.serverlessreviewtool.eventlisteners.SetOnFilterEvent;
import com.kalynx.serverlessreviewtool.theme.components.ThemedComboBox;
import com.kalynx.serverlessreviewtool.theme.components.ThemedLabel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedPanel;
import com.kalynx.serverlessreviewtool.theme.components.ThemedTextField;
import com.kalynx.serverlessreviewtool.utils.DebounceTimer;
import net.miginfocom.swing.MigLayout;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.*;

public class FilterPanel extends ThemedPanel {

    private final Set<SetOnFilterEvent> filterEventListeners = new HashSet<>();
    private DebounceTimer debounceTimer;

    // temporary mock data
    String[] repositoryData = {"All Repositories", "frontend-app", "backend-api", "mobile-app", "shared-lib"};

    // fields
    private final ThemedLabel titleLabel = new ThemedLabel("Title:")
            .setThemedToolTipText("Filter reviews by title");
    private final ThemedTextField titleFilterTextField = new ThemedTextField(20);
    private final ThemedLabel authorLabel = new ThemedLabel("Author:")
            .setThemedToolTipText("Filter reviews by author");
    private final ThemedTextField authorFilterTextField = new ThemedTextField(20);
    private final ThemedLabel repositoryLabel = new ThemedLabel("Repository:")
            .setThemedToolTipText("Filter reviews by repository");
    private final ThemedComboBox<String> repositories = new ThemedComboBox<>(repositoryData);

    public FilterPanel() {
        configureLayout();
        setupEventListeners();
    }

    private void setupEventListeners() {
        debounceTimer = new DebounceTimer(300, this::notifyFilterEventListener);

        DocumentListener textFieldListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounceTimer.trigger();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounceTimer.trigger();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounceTimer.trigger();
            }
        };

        titleFilterTextField.getDocument().addDocumentListener(textFieldListener);
        authorFilterTextField.getDocument().addDocumentListener(textFieldListener);

        repositories.addActionListener(e -> debounceTimer.trigger());
    }

    private void configureLayout() {
        setLayout(new MigLayout("","[][][][][][]","[]"));
        add(titleLabel, "cell 0 0");
        add(titleFilterTextField, "cell 1 0");
        add(authorLabel, "cell 2 0");
        add(authorFilterTextField, "cell 3 0");
        add(repositoryLabel, "cell 4 0");
        add(repositories, "cell 5 0");
    }

    public FilterPanel addFilterEventListener( SetOnFilterEvent eventListener) {
        filterEventListeners.add(eventListener);
        return this;
    }

    public FilterPanel removeFilterEventListener( SetOnFilterEvent eventListener) {
        filterEventListeners.remove(eventListener);
        return this;
    }

    private void notifyFilterEventListener() {
        String selectedRepo = (String) repositories.getSelectedItem();
        List<String> repoFilter = null;

        // If "All Repositories" is selected, pass null (no filter)
        // Otherwise, create a list with the selected repository
        if (selectedRepo != null && !selectedRepo.equals("All Repositories")) {
            repoFilter = Collections.singletonList(selectedRepo);
        }

        String titleFilter = titleFilterTextField.getText();
        String authorFilter = authorFilterTextField.getText();

        List<String> finalRepoFilter = repoFilter;
        filterEventListeners.forEach(listener ->
                listener.setFilterEventAction(titleFilter, authorFilter, finalRepoFilter));
    }

}
