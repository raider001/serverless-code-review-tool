# Coding Instructions for Serverless Review Tool

## General Principles

- Do not add comments to the code.
- Add Javadoc to all public classes and public methods.
- Keep methods focused and small - extract complex logic into helper methods.
- Use composition over inheritance.
- Follow existing design patterns consistently.

## Code Organization

### Panel Structure
- Break complex panels into child components in subfolders (e.g., `reviewpanel/`, `settingspanel/`).
- Main panels should be clean containers that compose child panels.
- Follow the pattern: Constructor → `configureLayout()` → `setupListeners()` → `loadSettings()`.
- Use local variables instead of fields if values are never accessed after initialization.

### Naming Conventions
- Use clear, descriptive names without redundant suffixes.
- Panel fields: `headerPanel`, `footerPanel` (not `headerPanelComponent`).
- Button fields: `editButton`, `saveButton` (not `editBtn`, `saveBtn`).
- Listener methods: `onEventName` (e.g., `onFileSelected`, `onCommitChanged`).
- Boolean methods: `isEnabled()`, `hasSelection()`, `canSave()`.

### File Organization
```
ui/
  mainpanels/
    SettingsPanel.java           # Main container
    ReviewSelectionPanel.java    # Main container
    settingspanel/               # Child components
      WindowSettingsPanel.java
      PollingSettingsPanel.java
      RepositoriesPanel.java
    reviewselectionpanel/        # Child components
      FilterPanel.java
      ReviewListPanel.java
```

## Theming

### Core Principles
- **All theme management must be in themed components, never in implementors.**
- Themed components handle their own appearance updates.
- Using a themed component means you never manually manage its colors/fonts.

### ThemedPanel Usage
- Extend `ThemedPanel` for all custom panels.
- ThemedPanel automatically updates titled borders during paint.
- Never override `paintComponent()` to manually update borders in implementing panels.

### Titled Borders
```java
// Good: Set once in constructor
public class MyPanel extends ThemedPanel {
    public MyPanel() {
        setBorder(ThemedTitledBorder.create("My Title"));
        configureLayout();
    }
}

// Bad: Never do this in implementations
@Override
protected void paintComponent(Graphics g) {
    setBorder(ThemedTitledBorder.create("My Title")); // Wrong!
    super.paintComponent(g);
}
```

### Paint Cycle Rules
- **Never modify component state during paint cycles.**
- No `setFont()`, `setBackground()`, `setForeground()`, `setBorder()` inside `paintComponent()`.
- Calculate colors/fonts outside paint, then only render during paint.
- Override `paint()` instead of `paintComponent()` if you need to temporarily set state.

## Component Design

### Field Declarations
```java
// Good: Inline initialization with final fields
private final SettingsManager settingsManager = SettingsManager.getInstance();
private final ThemedSpinner intervalSpinner = new ThemedSpinner(new SpinnerNumberModel(15, 1, 1440, 1));
private final ThemedCheckBox enableCheckBox = new ThemedCheckBox("Enable", true);

// Good: Local variables when not accessed after initialization
private JPanel createContentPanel() {
    WindowSettingsPanel windowSettings = new WindowSettingsPanel();
    PollingSettingsPanel polling = new PollingSettingsPanel();
    contentPanel.add(windowSettings, "grow, wrap");
    contentPanel.add(polling, "grow");
    return contentPanel;
}
```

### Constructor Pattern
```java
public MyPanel() {
    setBorder(ThemedTitledBorder.create("Title"));
    configureLayout();
    setupListeners();
    loadSettings();
}

private void configureLayout() {
    setLayout(new MigLayout("", "[][]", "[]10[]"));
    add(labelField, "cell 0 0");
    add(inputField, "cell 1 0");
}

private void setupListeners() {
    button.addActionListener(e -> handleButtonClick());
}

private void loadSettings() {
    inputField.setText(settingsManager.getSomeSetting());
}
```

## Listeners and Events

### ListenerFactory Usage
```java
// Good: Use ListenerFactory for common patterns
private void setupListeners() {
    textField.addFocusListener(ListenerFactory.createFocusLostAdapter(e -> saveValue()));
    
    addSpinnerFocusListener(intervalSpinner, () -> {
        int value = (Integer) intervalSpinner.getValue();
        settingsManager.updateValue(value);
    });
}

private void addSpinnerFocusListener(JSpinner spinner, Runnable onFocusLost) {
    JComponent editor = spinner.getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
        ((JSpinner.DefaultEditor) editor).getTextField()
            .addFocusListener(ListenerFactory.createFocusLostAdapter(e -> onFocusLost.run()));
    }
}
```

### Selection Listeners
```java
// Good: Check getValueIsAdjusting() to prevent multiple firings
list.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        updateButtonStates();
    }
});
```

### Callback Patterns
```java
// Good: Use functional interfaces for callbacks
public void setOnEditListener(ActionListener listener) {
    editButton.addActionListener(listener);
}

public void setOnStatusChangeListener(Runnable listener) {
    this.statusChangeListener = listener;
}
```

## Validation

### Using Validator Framework
```java
// Good: Use ThemedTextField.setupValidation()
private void setupValidation() {
    urlField.setupValidation(
        this::validateUrl,
        value -> settingsManager.updateUrl(value)
    );
}

private Validator.ValidationResult validateUrl(String urlString) {
    if (urlString.isEmpty()) {
        return Validator.ValidationResult.valid();
    }
    
    String urlPattern = "^(https?|wss?)://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$";
    
    return urlString.matches(urlPattern)
        ? Validator.ValidationResult.valid()
        : Validator.ValidationResult.invalid("Invalid URL format.");
}
```

## Layout Management

### Layout Selection Guidelines
- **Prefer MigLayout** for most panel layouts - it's flexible, powerful, and handles complex requirements
- **Use BorderLayout** only for simple container layouts (header/content/footer)
- **Consider refactoring** BoxLayout, FlowLayout, GridBagLayout, or GridLayout to MigLayout for consistency
- MigLayout provides better spacing control, alignment, and responsive behavior

### When to Refactor to MigLayout
```java
// Refactor these patterns to MigLayout:
setLayout(new FlowLayout(FlowLayout.LEFT));        // Complex forms
setLayout(new GridBagLayout());                    // Any GridBagLayout usage
setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // Multiple components with gaps

// Keep BorderLayout for:
setLayout(new BorderLayout());                     // Simple 3-5 section layouts
```

### MigLayout Patterns
```java
// Row and column constraints, with gaps
setLayout(new MigLayout("", "[][]30lp[][]", "[]10[]"));

// Common layout constraints
setLayout(new MigLayout("fill", "", ""));          // Fill parent container
setLayout(new MigLayout("insets 10", "", ""));     // 10px insets on all sides
setLayout(new MigLayout("hidemode 3", "", ""));    // Invisible components don't take space

// Common component constraints
add(component, "grow");           // Fill available space
add(component, "grow, wrap");     // Fill and wrap to next row
add(component, "cell 0 0");       // Specific cell (col 0, row 0)
add(component, "growx");          // Grow horizontally only
add(component, "growy");          // Grow vertically only
add(component, "span 3");         // Span 3 columns
add(component, "grow, pushy");    // Fill and push remaining space
add(component, "align right");    // Align to right
add(component, "wrap 10px");      // Wrap with 10px gap before next row

// Typical form layout
setLayout(new MigLayout("", "[][grow]", ""));
add(new ThemedLabel("Name:"), "");
add(nameField, "growx, wrap");
add(new ThemedLabel("Email:"), "");
add(emailField, "growx, wrap");
```

### BorderLayout for Simple Containers
```java
// Good: Simple container layout with 3-5 regions
setLayout(new BorderLayout());
add(headerPanel, BorderLayout.NORTH);
add(contentPanel, BorderLayout.CENTER);
add(footerPanel, BorderLayout.SOUTH);

// Good: Split pane wrappers
setLayout(new BorderLayout());
add(splitPane, BorderLayout.CENTER);
```

## Button State Management

### Pattern
```java
private void setupActions() {
    updateButtonStates();
    
    list.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            updateButtonStates();
        }
    });
    
    addButton.addActionListener(e -> handleAdd());
    editButton.addActionListener(e -> handleEdit());
    removeButton.addActionListener(e -> handleRemove());
}

private void updateButtonStates() {
    boolean hasSelection = list.getSelectedIndex() >= 0;
    editButton.setEnabled(hasSelection);
    removeButton.setEnabled(hasSelection);
}
```

### Guidelines
- Always initialize button states in `setupActions()` or similar method
- Update button states after any action that changes selection or data
- Use boolean flags for readability: `boolean hasSelection = ...`
- Disable buttons that require selection when nothing is selected
- Use `getValueIsAdjusting()` check before updating states in list selection listeners

## Dialogs

### Confirmation Dialogs
```java
// Good: Use ThemedConfirmDialog
boolean confirmed = ThemedConfirmDialog.showConfirmation(
    SwingUtilities.getWindowAncestor(this),
    "Confirm Delete",
    "Are you sure you want to remove '" + item.getName() + "'?"
);

if (confirmed) {
    performDelete();
}
```

### Custom Dialogs
```java
// Good: Check isConfirmed() before applying changes
MyDialog dialog = new MyDialog(parentWindow, context);
dialog.setVisible(true);

if (dialog.isConfirmed()) {
    dialog.applyTo(context);
    refreshUI();
}
```

## Data Synchronization

### Manager Pattern
```java
// Good: Keep UI model synchronized with manager
private void addRepository(AppSettings.RepositoryConfig config) {
    settingsManager.getSettings().getRepositories().add(config);
    repositoryListModel.addElement(config);
    settingsManager.saveSettings();
}

private void updateRepository(int index, AppSettings.RepositoryConfig updated) {
    settingsManager.getSettings().getRepositories().set(index, updated);
    repositoryListModel.setElementAt(updated, index);
    settingsManager.saveSettings();
}

private void removeRepository(int index) {
    settingsManager.getSettings().getRepositories().remove(index);
    repositoryListModel.remove(index);
    settingsManager.saveSettings();
}
```

## Common Anti-Patterns

### Avoid These
```java
// Bad: Modifying state during paint
@Override
protected void paintComponent(Graphics g) {
    setFont(getFont().deriveFont(Font.BOLD));  // Never!
    setBackground(color);                       // Never!
    super.paintComponent(g);
}

// Bad: Manual theme management in implementations
@Override
protected void paintComponent(Graphics g) {
    setBorder(ThemedTitledBorder.create("Title")); // Wrong!
    super.paintComponent(g);
}

// Bad: Storing references that are never used
private final ThemedPanel panel = new ThemedPanel(); // Remove if never accessed

// Bad: Not checking getValueIsAdjusting()
list.addListSelectionListener(e -> {
    updateUI(); // Will fire multiple times!
});

// Bad: Complex validation logic in UI classes
private boolean isValid(String url) {
    // 50 lines of validation logic... Extract to validator!
}
```

## Best Practices Summary

1. **Keep classes focused** - One responsibility per class.
2. **Extract complex logic** - Helper methods or separate classes.
3. **Use final fields** - Immutability where possible.
4. **Minimize constructor code** - Call helper methods instead.
5. **Never modify state during paint** - Calculate outside, render inside.
6. **Let themed components handle theming** - Don't override in implementations.
7. **Use functional interfaces** - Runnable, Consumer, Supplier for callbacks.
8. **Validate early** - Use Validator framework for user input.
9. **Test frequently** - Compile and check errors after each change.
10. **Follow existing patterns** - Consistency is key.
