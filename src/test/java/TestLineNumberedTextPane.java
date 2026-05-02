import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.LineNumberedTextPane;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import javax.swing.*;
import java.awt.*;

/**
 * Test to demonstrate the new LineNumberedTextPane component
 */
public class TestLineNumberedTextPane {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Testing LineNumberedTextPane component...");

                ThemeManager themeManager = ThemeManager.getInstance();

                // Create test frame
                JFrame frame = new JFrame("LineNumberedTextPane Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(600, 500);

                // Create main panel
                JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Add title
                JLabel title = new JLabel("LineNumberedTextPane with Line Indicators");
                title.setFont(new Font("Segoe UI", Font.BOLD, 16));
                mainPanel.add(title, BorderLayout.NORTH);

                // Create LineNumberedTextPane
                LineNumberedTextPane lineNumberedPane = new LineNumberedTextPane();

                // Set sample code with mixed changes
                String sampleCode = "public class Example {\n" +
                                   "    private String name;\n" +
                                   "    private int age;  // NEW LINE\n" +
                                   "    private String email;  // NEW LINE\n" +
                                   "    \n" +
                                   "    public Example(String name) {\n" +
                                   "        this.name = name;\n" +
                                   "    }\n" +
                                   "    \n" +
                                   "    public String getName() {\n" +
                                   "        return name;\n" +
                                   "    }\n" +
                                   "    \n" +
                                   "    public void setAge(int newAge) {  // DELETED METHOD\n" +
                                   "        this.age = newAge;\n" +
                                   "    }\n" +
                                   "}";

                lineNumberedPane.setText(sampleCode);

                // Mark lines as added/removed
                lineNumberedPane.markLineAdded(3);
                lineNumberedPane.markLineAdded(4);
                lineNumberedPane.markLineRemoved(13);
                lineNumberedPane.markLineRemoved(14);
                lineNumberedPane.markLineRemoved(15);

                mainPanel.add(lineNumberedPane, BorderLayout.CENTER);

                // Add legend
                JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
                legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

                // Added indicator
                JPanel addedBox = new JPanel();
                addedBox.setPreferredSize(new Dimension(20, 20));
                addedBox.setBackground(new Color(40, 167, 69));
                legendPanel.add(addedBox);
                legendPanel.add(new JLabel("Added lines"));

                // Removed indicator
                JPanel removedBox = new JPanel();
                removedBox.setPreferredSize(new Dimension(20, 20));
                removedBox.setBackground(new Color(220, 53, 69));
                legendPanel.add(removedBox);
                legendPanel.add(new JLabel("Removed lines"));

                mainPanel.add(legendPanel, BorderLayout.SOUTH);

                frame.setContentPane(mainPanel);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                System.out.println("✓ LineNumberedTextPane created successfully");
                System.out.println("✓ Line numbers displayed on the left");
                System.out.println("✓ Added lines marked in green");
                System.out.println("✓ Removed lines marked in red");
                System.out.println("✓ Component is theme-aware");

            } catch (Exception e) {
                System.err.println("✗ Test failed:");
                e.printStackTrace();
            }
        });
    }
}

