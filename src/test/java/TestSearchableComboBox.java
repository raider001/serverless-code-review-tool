import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.*;
import com.kalynx.serverlessreviewtool.swingextensions.components.SearchableCombobox;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSearchableComboBox {

    private static boolean isDarkTheme = true;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.applyTheme();

            JFrame frame = new JFrame("Searchable ComboBox Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            ThemedPanel mainPanel = new ThemedPanel();
            mainPanel.setLayout(new BorderLayout(10, 10));

            ThemedPanel headerPanel = new ThemedPanel();
            headerPanel.setLayout(new BorderLayout());
            ThemedLabel titleLabel = new ThemedLabel("Searchable Multi-Select Demo");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headerPanel.add(titleLabel, BorderLayout.WEST);

            ThemedButton themeToggle = new ThemedButton("Toggle Theme");
            themeToggle.addActionListener(e -> {
                isDarkTheme = !isDarkTheme;
                if (isDarkTheme) {
                    themeManager.setDarkTheme();
                } else {
                    themeManager.setLightTheme();
                }
                frame.repaint();
            });
            headerPanel.add(themeToggle, BorderLayout.EAST);

            ThemedPanel demoPanel = createDemoPanel();

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(demoPanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static ThemedPanel createDemoPanel() {
        ThemedPanel panel = new ThemedPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(ThemedTitledBorder.create("Select Technologies"));

        List<String> selectedItems = new ArrayList<>();

        ThemedPanel topPanel = new ThemedPanel();
        topPanel.setLayout(new BorderLayout(5, 5));

        ThemedPanel badgesPanel = new ThemedPanel();
        badgesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badgesPanel.setOpaque(false);

        ThemedScrollPane badgeScroll = new ThemedScrollPane(badgesPanel);
        badgeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        badgeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        badgeScroll.setPreferredSize(new Dimension(0, 50));
        badgeScroll.setBorder(BorderFactory.createEmptyBorder());
        topPanel.add(badgeScroll, BorderLayout.NORTH);

        List<String> technologies = Arrays.asList(
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#",
            "Ruby", "PHP", "Swift", "Kotlin", "Scala", "Haskell", "Elixir",
            "React", "Vue", "Angular", "Spring Boot", "Django", "Flask", "Express",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "PostgreSQL", "MongoDB"
        );

        ThemedPanel selectorPanel = new ThemedPanel();
        selectorPanel.setLayout(new BorderLayout(5, 5));

        SearchableCombobox<String> searchBox = new SearchableCombobox<>();
        searchBox.setValues(technologies);
        searchBox.setToolTipText("Type to search and select technologies...");

        searchBox.setOnApply(selected -> {
            if (selected != null && !selectedItems.contains(selected)) {
                selectedItems.add(selected);
                updateBadges(badgesPanel, selectedItems);
            }
        });

        ThemedButton addButton = new ThemedButton("Add");
        addButton.setPreferredSize(new Dimension(70, 28));
        addButton.addActionListener(e -> {
            Object selected = searchBox.getSelectedItem();
            if (selected != null && !selectedItems.contains(selected.toString())) {
                selectedItems.add(selected.toString());
                updateBadges(badgesPanel, selectedItems);
            }
        });

        selectorPanel.add(searchBox, BorderLayout.CENTER);
        selectorPanel.add(addButton, BorderLayout.EAST);
        topPanel.add(selectorPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        ThemedTextArea infoArea = new ThemedTextArea(8, 40);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText(
            "Instructions:\n\n" +
            "1. Type in the combo box to filter items (filters by prefix)\n" +
            "2. Navigate with arrow keys (UP/DOWN)\n" +
            "3. Press ENTER to add selected item as badge\n" +
            "4. OR click 'Add' button to add current selection\n" +
            "5. Click a badge to remove it\n" +
            "6. Press ESCAPE to close dropdown\n\n" +
            "Try typing: 'Jav', 'Py', 'Doc', etc.\n" +
            "Filters by how items START (not contain)"
        );
        ThemedScrollPane infoScroll = new ThemedScrollPane(infoArea);
        panel.add(infoScroll, BorderLayout.CENTER);

        return panel;
    }

    private static void updateBadges(ThemedPanel panel, List<String> items) {
        panel.removeAll();
        for (String item : items) {
            panel.add(new ThemedBadge(item, () -> {
                items.remove(item);
                updateBadges(panel, items);
            }));
        }
        panel.revalidate();
        panel.repaint();
        Container p = panel.getParent();
        while (p != null) {
            p.revalidate();
            p.repaint();
            p = p.getParent();
        }
    }
}

